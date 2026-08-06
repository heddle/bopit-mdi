package edu.cnu.bopit.calculation;

import edu.cnu.bopit.grid.AdaptiveCoulombGridFactory;
import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.DiracHamiltonianBuilder;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.InverseIterationSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.physics.DiracCoulombEnergy;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.solver.InverseIterationSolver;

/** Point/electromagnetic coupled Dirac calculation. */
public final class DiracCalculator {
    public DiracResult calculate(BopitProblem problem, PhysicalConstantSet constants,
            CalculationMonitor monitor) {
        if (!(problem.waveEquation() instanceof DiracSpec dirac)) {
            throw new IllegalArgumentException("DiracSpec is required");
        }
        if (!(problem.strongInteraction() instanceof NoStrongInteractionSpec)) {
            throw new IllegalArgumentException("Dirac strong interactions are not implemented");
        }
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        MomentumGrid grid = createGrid(problem, constants);
        var matrix = new DiracHamiltonianBuilder().build(dirac,
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants,
                problem.electromagnetic());
        var ordinary = problem.solver();
        var doubledSpec = new InverseIterationSpec(ordinary.shiftMeV(),
                ordinary.energyTolerance(), ordinary.residualTolerance(),
                ordinary.minimumIterations(), ordinary.maximumIterations());
        var solver = new InverseIterationSolver().solve(matrix, doubledSpec, monitor);
        double mu = ReducedMass.of(problem.atomicSystem().particleMassMeV(),
                problem.atomicSystem().nuclearMassMeV());
        double exact = DiracCoulombEnergy.bindingEnergyMeV(mu,
                problem.atomicSystem().nuclearCharge(), constants.fineStructureConstant(),
                problem.quantumState().principalN(), dirac.kappa());
        double[] vector = solver.eigenvector();
        double largeNorm = 0.0;
        double smallNorm = 0.0;
        for (int i = 0; i < grid.size(); i++) {
            largeNorm += vector[i] * vector[i];
            smallNorm += vector[i + grid.size()] * vector[i + grid.size()];
        }
        double totalNorm = largeNorm + smallNorm;
        largeNorm /= totalNorm;
        smallNorm /= totalNorm;
        return new DiracResult(solver.eigenvalueMeV(), exact,
                Math.abs(solver.eigenvalueMeV() - exact), grid, solver, largeNorm, smallNorm);
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
