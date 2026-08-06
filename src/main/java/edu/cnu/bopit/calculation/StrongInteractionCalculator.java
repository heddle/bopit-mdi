package edu.cnu.bopit.calculation;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.AdaptiveCoulombGridFactory;
import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.ComplexMatrix;
import edu.cnu.bopit.matrix.SchrodingerHamiltonianBuilder;
import edu.cnu.bopit.matrix.StrongInteractionMatrixBuilder;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.strong.StrongInteractionFactory;
import edu.cnu.bopit.physics.wavefunction.ComplexWavefunctionFactory;
import edu.cnu.bopit.solver.ComplexInverseIterationSolver;
import edu.cnu.bopit.solver.InverseIterationSolver;

/** Orchestrates the Stage 7 complex Schrödinger vertical slice. */
public final class StrongInteractionCalculator {
    public StrongInteractionResult calculate(BopitProblem problem, PhysicalConstantSet constants,
            ComplexInverseIterationSpec solverSpec, CalculationMonitor monitor) {
        if (problem == null || constants == null || solverSpec == null) {
            throw new IllegalArgumentException("problem, constants, and complex solver specification are required");
        }
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        monitor.progress(0.0, "Constructing momentum grid");
        MomentumGrid grid = createGrid(problem, constants);
        var electromagnetic = new SchrodingerHamiltonianBuilder().build(
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants,
                problem.electromagnetic());
        monitor.checkCancelled();
        monitor.progress(0.25, "Assembling complex strong interaction");
        var model = StrongInteractionFactory.create(problem.strongInteraction(),
                problem.atomicSystem(), constants);
        ComplexMatrix strong = new StrongInteractionMatrixBuilder().build(
                problem.quantumState().orbitalL(), grid, model);
        ComplexMatrix hamiltonian = new ComplexMatrix(
                electromagnetic.matrixMeV().add(strong.real()), strong.imaginary());
        monitor.checkCancelled();
        monitor.progress(0.5, "Solving complex bound state");
        CalculationMonitor outer = monitor;
        var solver = new ComplexInverseIterationSolver().solve(hamiltonian, solverSpec,
                new CalculationMonitor() {
                    @Override public boolean isCancellationRequested() { return outer.isCancellationRequested(); }
                    @Override public void progress(double fraction, String message) {
                        outer.progress(0.5 + 0.4 * fraction, message);
                    }
                });
        double reference = new InverseIterationSolver().solve(electromagnetic.matrixMeV(),
                problem.solver(), CalculationMonitor.NONE).eigenvalueMeV();
        Complex energy = solver.eigenvalueMeV();
        double shift = energy.getReal() - reference;
        double width = -2.0 * energy.getImaginary();
        monitor.progress(0.92, "Transforming complex wavefunction");
        var wavefunctions = ComplexWavefunctionFactory.create(problem.quantumState().orbitalL(),
                grid, solver.eigenvector());
        monitor.progress(1.0, "Complex calculation complete");
        return new StrongInteractionResult(reference, energy, shift, width, grid,
                hamiltonian, strong, solver, wavefunctions);
    }

    private static MomentumGrid createGrid(BopitProblem problem, PhysicalConstantSet constants) {
        if (problem.grid() instanceof LegacyGridSpec legacy) return new LegacyMappedGridFactory().create(legacy);
        if (problem.grid() instanceof AdaptiveGridSpec adaptive) {
            return new AdaptiveCoulombGridFactory().create(adaptive, problem.atomicSystem(),
                    problem.quantumState(), constants);
        }
        throw new IllegalArgumentException("unsupported grid specification: " + problem.grid());
    }
}
