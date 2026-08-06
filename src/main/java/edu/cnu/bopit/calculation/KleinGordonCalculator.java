package edu.cnu.bopit.calculation;

import java.util.ArrayList;

import edu.cnu.bopit.grid.AdaptiveCoulombGridFactory;
import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.KleinGordonHamiltonianBuilder;
import edu.cnu.bopit.matrix.SchrodingerHamiltonianBuilder;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.InverseIterationSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.physics.KleinGordonCoulombEnergy;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.wavefunction.PointCoulombWavefunctionFactory;
import edu.cnu.bopit.solver.InverseIterationResult;
import edu.cnu.bopit.solver.InverseIterationSolver;

/** Outer/inner solver for the real electromagnetic Klein-Gordon equation. */
public final class KleinGordonCalculator {
    public KleinGordonResult calculate(BopitProblem problem, PhysicalConstantSet constants,
            CalculationMonitor monitor) {
        if (!(problem.waveEquation() instanceof KleinGordonSpec kg)) {
            throw new IllegalArgumentException("KleinGordonSpec is required");
        }
        if (!(problem.strongInteraction() instanceof NoStrongInteractionSpec)) {
            throw new IllegalArgumentException("complex nuclear Klein-Gordon coupling is not implemented yet");
        }
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        MomentumGrid grid = createGrid(problem, constants);
        // Reuse the validated electromagnetic assembly; kinetic is discarded below.
        var emSystem = new SchrodingerHamiltonianBuilder().build(new SchrodingerSpec(),
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants,
                problem.electromagnetic());
        var potential = emSystem.coulombOperatorMeV()
                .add(emSystem.finiteSizeCorrectionMeV()).add(emSystem.vacuumPolarizationMeV());
        double mu = ReducedMass.of(problem.atomicSystem().particleMassMeV(),
                problem.atomicSystem().nuclearMassMeV());
        double binding = problem.solver().shiftMeV();
        var history = new ArrayList<KleinGordonOuterRecord>();
        InverseIterationResult inner = null;
        boolean converged = false;
        for (int cycle = 1; cycle <= kg.maximumOuterIterations(); cycle++) {
            monitor.checkCancelled();
            var builder = new KleinGordonHamiltonianBuilder();
            var matrix = builder.build(binding, mu, grid, potential);
            builder.addKinetic(matrix, grid, constants.hbarCMeVFm());
            double epsilonGuess = binding * (binding + 2.0 * mu);
            double scale = Math.max(1.0, 2.0 * (mu + binding));
            var innerSpec = new InverseIterationSpec(epsilonGuess,
                    problem.solver().energyTolerance() * scale,
                    problem.solver().residualTolerance() * scale,
                    problem.solver().minimumIterations(), problem.solver().maximumIterations());
            inner = new InverseIterationSolver().solve(matrix, innerSpec, CalculationMonitor.NONE);
            double epsilon = inner.eigenvalueMeV();
            double root = Math.sqrt(mu * mu + epsilon);
            double next = epsilon / (root + mu);
            double change = Math.abs(next - binding);
            double residual = inner.history().get(inner.history().size() - 1).residualNorm();
            history.add(new KleinGordonOuterRecord(cycle, binding, epsilon, next, change,
                    inner.history().size(), residual));
            monitor.progress((double) cycle / kg.maximumOuterIterations(),
                    "Klein-Gordon outer cycle " + cycle);
            binding = next;
            if (inner.converged() && change <= kg.outerToleranceMeV()) {
                converged = true;
                break;
            }
        }
        double exact = KleinGordonCoulombEnergy.bindingEnergyMeV(mu,
                problem.atomicSystem().nuclearCharge(), constants.fineStructureConstant(),
                problem.quantumState().principalN(), problem.quantumState().orbitalL());
        var wavefunctions = PointCoulombWavefunctionFactory.create(problem.atomicSystem(),
                problem.quantumState(), constants, grid, inner.eigenvector());
        return new KleinGordonResult(converged, binding, exact, grid, history, inner,
                wavefunctions, converged ? "outer and inner convergence" : "maximum outer cycles reached");
    }

    private static MomentumGrid createGrid(BopitProblem problem, PhysicalConstantSet constants) {
        if (problem.grid() instanceof LegacyGridSpec legacy) return new LegacyMappedGridFactory().create(legacy);
        if (problem.grid() instanceof AdaptiveGridSpec adaptive) {
            return new AdaptiveCoulombGridFactory().create(adaptive, problem.atomicSystem(),
                    problem.quantumState(), constants);
        }
        throw new IllegalArgumentException("unsupported grid specification");
    }
}
