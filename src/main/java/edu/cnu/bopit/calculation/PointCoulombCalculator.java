package edu.cnu.bopit.calculation;

import edu.cnu.bopit.grid.AdaptiveCoulombGridFactory;
import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.HamiltonianSystem;
import edu.cnu.bopit.matrix.SchrodingerHamiltonianBuilder;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.solver.InverseIterationResult;
import edu.cnu.bopit.solver.InverseIterationSolver;
import edu.cnu.bopit.physics.wavefunction.PointCoulombWavefunctionFactory;

/** Orchestrates a complete headless point-Coulomb calculation. */
public final class PointCoulombCalculator {
    public PointCoulombResult calculate(BopitProblem problem,
            PhysicalConstantSet constants, CalculationMonitor monitor) {
        if (problem == null || constants == null) throw new IllegalArgumentException("problem and constants are required");
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        monitor.progress(0.0, "Constructing momentum grid");
        MomentumGrid grid;
        if (problem.grid() instanceof LegacyGridSpec legacy) {
            grid = new LegacyMappedGridFactory().create(legacy);
        } else if (problem.grid() instanceof AdaptiveGridSpec adaptive) {
            grid = new AdaptiveCoulombGridFactory().create(adaptive,
                    problem.atomicSystem(), problem.quantumState(), constants);
        } else {
            throw new IllegalArgumentException("unsupported grid specification: " + problem.grid());
        }
        monitor.checkCancelled();
        monitor.progress(0.15, "Assembling electromagnetic Hamiltonian");
        HamiltonianSystem system = new SchrodingerHamiltonianBuilder().build(
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants,
                problem.electromagnetic());
        monitor.checkCancelled();
        monitor.progress(0.5, "Solving selected bound state");
        CalculationMonitor outerMonitor = monitor;
        CalculationMonitor solverMonitor = new CalculationMonitor() {
            @Override public boolean isCancellationRequested() {
                return outerMonitor.isCancellationRequested();
            }
            @Override public void progress(double fraction, String message) {
                outerMonitor.progress(0.5 + 0.4 * fraction, message);
            }
        };
        InverseIterationResult solverResult = new InverseIterationSolver().solve(
                system.matrixMeV(), problem.solver(), solverMonitor);
        double reducedMass = ReducedMass.of(problem.atomicSystem().particleMassMeV(),
                problem.atomicSystem().nuclearMassMeV());
        double reference = ReducedMass.exactCoulombEnergyMeV(reducedMass,
                problem.atomicSystem().nuclearCharge(), constants.fineStructureConstant(),
                problem.quantumState().principalN());
        double calculated = solverResult.eigenvalueMeV();
        double absolute = Math.abs(calculated - reference);
        monitor.checkCancelled();
        monitor.progress(0.92, "Normalizing and transforming wavefunction");
        var wavefunctions = PointCoulombWavefunctionFactory.create(problem.atomicSystem(),
                problem.quantumState(), constants, grid, solverResult.eigenvector());
        monitor.progress(1.0, "Calculation complete");
        double finiteSizeExpectation = expectation(solverResult.eigenvector(),
                system.finiteSizeCorrectionMeV());
        double vacuumExpectation = expectation(solverResult.eigenvector(),
                system.vacuumPolarizationMeV());
        return new PointCoulombResult(reducedMass, reference, calculated, absolute,
                absolute / Math.abs(reference), grid, system.landeDiagnostics(), solverResult,
                system.matrixMeV(), system.coulombOperatorMeV(),
                system.finiteSizeCorrectionMeV(), system.vacuumPolarizationMeV(),
                finiteSizeExpectation, vacuumExpectation, wavefunctions);
    }

    private static double expectation(double[] vector, org.apache.commons.math3.linear.RealMatrix matrix) {
        double sum = 0.0;
        for (int i = 0; i < vector.length; i++) {
            for (int j = 0; j < vector.length; j++) sum += vector[i] * matrix.getEntry(i, j) * vector[j];
        }
        return sum;
    }
}
