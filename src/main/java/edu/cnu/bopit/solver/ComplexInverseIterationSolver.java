package edu.cnu.bopit.solver;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.matrix.ComplexBlockMatrices;
import edu.cnu.bopit.matrix.ComplexMatrix;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;

/** Complex shifted inverse iteration solved through the tested real-block representation. */
public final class ComplexInverseIterationSolver {
    public ComplexInverseIterationResult solve(ComplexMatrix hamiltonian,
            ComplexInverseIterationSpec spec, CalculationMonitor monitor) {
        if (hamiltonian == null || spec == null) throw new IllegalArgumentException("matrix and spec are required");
        if (hamiltonian.rows() != hamiltonian.columns()) throw new IllegalArgumentException("matrix must be square");
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        int n = hamiltonian.rows();
        var decomposition = new LUDecomposition(
                ComplexBlockMatrices.shiftedRealBlock(hamiltonian, spec.shiftMeV()));
        if (!decomposition.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("shifted complex Hamiltonian is singular");
        }
        RealVector vector = new ArrayRealVector(2 * n);
        for (int i = 0; i < n; i++) {
            vector.setEntry(i, 1.0 / Math.sqrt(n));
            vector.setEntry(i + n, (i + 1.0) * 1e-6 / n);
        }
        vector = vector.mapDivide(vector.getNorm());
        var history = new ArrayList<ComplexIterationRecord>();
        Complex previous = null;
        Complex eigenvalue = spec.shiftMeV();
        for (int iteration = 1; iteration <= spec.maximumIterations(); iteration++) {
            monitor.checkCancelled();
            vector = decomposition.getSolver().solve(vector);
            vector = vector.mapDivide(vector.getNorm());
            Complex[] complexVector = unpack(vector, n);
            eigenvalue = rayleigh(hamiltonian, complexVector);
            double residual = residualNorm(hamiltonian, complexVector, eigenvalue);
            double change = previous == null ? Double.POSITIVE_INFINITY : eigenvalue.subtract(previous).abs();
            history.add(new ComplexIterationRecord(iteration, eigenvalue, change, residual));
            monitor.progress((double) iteration / spec.maximumIterations(),
                    "Complex inverse iteration " + iteration);
            if (iteration >= spec.minimumIterations() && change <= spec.energyToleranceMeV()
                    && residual <= spec.residualTolerance()) {
                return new ComplexInverseIterationResult(true, eigenvalue, complexVector,
                        history, "converged");
            }
            previous = eigenvalue;
        }
        return new ComplexInverseIterationResult(false, eigenvalue, unpack(vector, n),
                history, "maximum iterations reached");
    }

    private static Complex rayleigh(ComplexMatrix matrix, Complex[] vector) {
        Complex numerator = Complex.ZERO;
        double denominator = 0.0;
        for (int i = 0; i < vector.length; i++) {
            Complex row = Complex.ZERO;
            for (int j = 0; j < vector.length; j++) row = row.add(matrix.entry(i, j).multiply(vector[j]));
            numerator = numerator.add(vector[i].conjugate().multiply(row));
            denominator += vector[i].abs() * vector[i].abs();
        }
        return numerator.divide(denominator);
    }

    private static double residualNorm(ComplexMatrix matrix, Complex[] vector, Complex eigenvalue) {
        double sum = 0.0;
        for (int i = 0; i < vector.length; i++) {
            Complex row = Complex.ZERO;
            for (int j = 0; j < vector.length; j++) row = row.add(matrix.entry(i, j).multiply(vector[j]));
            double residual = row.subtract(eigenvalue.multiply(vector[i])).abs();
            sum += residual * residual;
        }
        return Math.sqrt(sum);
    }

    private static Complex[] unpack(RealVector vector, int n) {
        Complex[] result = new Complex[n];
        for (int i = 0; i < n; i++) result[i] = new Complex(vector.getEntry(i), vector.getEntry(i + n));
        return result;
    }
}
