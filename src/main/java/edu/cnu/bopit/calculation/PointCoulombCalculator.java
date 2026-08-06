package edu.cnu.bopit.calculation;

import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.HamiltonianSystem;
import edu.cnu.bopit.matrix.SchrodingerHamiltonianBuilder;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.solver.InverseIterationResult;
import edu.cnu.bopit.solver.InverseIterationSolver;

/** Orchestrates the complete headless Stage 1 calculation. */
public final class PointCoulombCalculator {
    public PointCoulombResult calculate(BopitProblem problem,
            PhysicalConstantSet constants, CalculationMonitor monitor) {
        if (problem == null || constants == null) throw new IllegalArgumentException("problem and constants are required");
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        monitor.progress(0.0, "Constructing legacy momentum grid");
        MomentumGrid grid = new LegacyMappedGridFactory().create(problem.grid());
        monitor.checkCancelled();
        monitor.progress(0.15, "Assembling Landé-subtracted Hamiltonian");
        HamiltonianSystem system = new SchrodingerHamiltonianBuilder().build(
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants);
        monitor.checkCancelled();
        monitor.progress(0.5, "Solving selected bound state");
        CalculationMonitor outerMonitor = monitor;
        CalculationMonitor solverMonitor = new CalculationMonitor() {
            @Override public boolean isCancellationRequested() {
                return outerMonitor.isCancellationRequested();
            }
            @Override public void progress(double fraction, String message) {
                outerMonitor.progress(0.5 + 0.5 * fraction, message);
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
        monitor.progress(1.0, "Calculation complete");
        return new PointCoulombResult(reducedMass, reference, calculated, absolute,
                absolute / Math.abs(reference), grid, system.landeDiagnostics(), solverResult);
    }
}
