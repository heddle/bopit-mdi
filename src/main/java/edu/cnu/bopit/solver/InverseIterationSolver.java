package edu.cnu.bopit.solver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.model.InverseIterationSpec;

/** Shifted inverse iteration using one LU factorization and repeated solves. */
public final class InverseIterationSolver {
    public InverseIterationResult solve(RealMatrix hamiltonian, InverseIterationSpec spec,
            CalculationMonitor monitor) {
        if (hamiltonian == null || !hamiltonian.isSquare()) {
            throw new IllegalArgumentException("hamiltonian must be square");
        }
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        int n = hamiltonian.getRowDimension();
        RealMatrix shifted = hamiltonian.copy();
        for (int i = 0; i < n; i++) shifted.addToEntry(i, i, -spec.shiftMeV());
        DecompositionSolver solver = new LUDecomposition(shifted).getSolver();
        if (!solver.isNonSingular()) throw new IllegalArgumentException("shifted Hamiltonian is singular");

        RealVector vector = new ArrayRealVector(n, 1.0);
        List<IterationRecord> history = new ArrayList<>();
        double previousEnergy = Double.NaN;
        double energy = Double.NaN;

        for (int iteration = 1; iteration <= spec.maximumIterations(); iteration++) {
            monitor.checkCancelled();
            RealVector solved = solver.solve(vector);
            int maxIndex = maxAbsIndex(solved);
            double normalizationComponent = solved.getEntry(maxIndex);
            if (!Double.isFinite(normalizationComponent) || normalizationComponent == 0.0) {
                throw new ArithmeticException("inverse iteration produced a zero or non-finite scale");
            }
            double historical = spec.shiftMeV() + 1.0 / normalizationComponent;
            vector = solved.mapDivide(normalizationComponent);
            RealVector hVector = hamiltonian.operate(vector);
            energy = vector.dotProduct(hVector) / vector.dotProduct(vector);
            double residual = hVector.subtract(vector.mapMultiply(energy)).getNorm() / vector.getNorm();
            double change = Double.isNaN(previousEnergy)
                    ? Double.POSITIVE_INFINITY : Math.abs(energy - previousEnergy);
            history.add(new IterationRecord(iteration, energy, historical, change,
                    residual, normalizationComponent));
            monitor.progress((double) iteration / spec.maximumIterations(), "Inverse iteration " + iteration);
            if (iteration >= spec.minimumIterations()
                    && change <= spec.energyTolerance()
                    && residual <= spec.residualTolerance()) {
                return new InverseIterationResult(true, energy, vector.toArray(), history, "converged");
            }
            previousEnergy = energy;
        }
        return new InverseIterationResult(false, energy, vector.toArray(), history,
                "maximum iterations reached");
    }

    private static int maxAbsIndex(RealVector vector) {
        int index = 0;
        double maximum = -1.0;
        for (int i = 0; i < vector.getDimension(); i++) {
            double magnitude = Math.abs(vector.getEntry(i));
            if (magnitude > maximum) {
                maximum = magnitude;
                index = i;
            }
        }
        return index;
    }
}
