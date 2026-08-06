package edu.cnu.bopit.calculation;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.AdaptiveCoulombGridFactory;
import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.ComplexKleinGordonHamiltonianBuilder;
import edu.cnu.bopit.matrix.SchrodingerHamiltonianBuilder;
import edu.cnu.bopit.matrix.StrongInteractionMatrixBuilder;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.strong.StrongInteractionFactory;
import edu.cnu.bopit.physics.wavefunction.ComplexWavefunctionFactory;
import edu.cnu.bopit.solver.ComplexInverseIterationResult;
import edu.cnu.bopit.solver.ComplexInverseIterationSolver;

/** Complex outer/inner Klein-Gordon solver for all four named nuclear couplings. */
public final class ComplexKleinGordonCalculator {
    public ComplexKleinGordonResult calculate(BopitProblem problem, PhysicalConstantSet constants,
            CalculationMonitor monitor) {
        if (!(problem.waveEquation() instanceof KleinGordonSpec kg)
                || problem.strongInteraction() instanceof NoStrongInteractionSpec) {
            throw new IllegalArgumentException("Klein-Gordon equation and a strong interaction are required");
        }
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        MomentumGrid grid = createGrid(problem, constants);
        var em = new SchrodingerHamiltonianBuilder().build(new SchrodingerSpec(),
                problem.quantumState().orbitalL(), grid, problem.atomicSystem(), constants,
                problem.electromagnetic());
        var coulomb = em.coulombOperatorMeV().add(em.finiteSizeCorrectionMeV())
                .add(em.vacuumPolarizationMeV());
        var nuclear = new StrongInteractionMatrixBuilder().build(problem.quantumState().orbitalL(),
                grid, StrongInteractionFactory.create(problem.strongInteraction(),
                        problem.atomicSystem(), constants));
        double mu = ReducedMass.of(problem.atomicSystem().particleMassMeV(),
                problem.atomicSystem().nuclearMassMeV());
        Complex binding = new Complex(problem.solver().shiftMeV(), -0.001);
        var history = new ArrayList<ComplexKleinGordonOuterRecord>();
        ComplexInverseIterationResult inner = null;
        boolean converged = false;
        for (int cycle = 1; cycle <= kg.maximumOuterIterations(); cycle++) {
            monitor.checkCancelled();
            var matrix = new ComplexKleinGordonHamiltonianBuilder().build(kg.form(), binding,
                    mu, grid, coulomb, nuclear, constants.hbarCMeVFm());
            Complex epsilonGuess = binding.multiply(binding.add(2.0 * mu));
            double scale = Math.max(1.0, 2.0 * binding.add(mu).abs());
            var innerSpec = new ComplexInverseIterationSpec(epsilonGuess,
                    problem.solver().energyTolerance() * scale,
                    problem.solver().residualTolerance() * scale,
                    problem.solver().minimumIterations(), problem.solver().maximumIterations());
            inner = new ComplexInverseIterationSolver().solve(matrix, innerSpec,
                    CalculationMonitor.NONE);
            Complex epsilon = inner.eigenvalueMeV();
            Complex root = epsilon.add(mu * mu).sqrt();
            if (root.getReal() < 0.0) root = root.negate();
            Complex next = epsilon.divide(root.add(mu));
            double change = next.subtract(binding).abs();
            double residual = inner.history().get(inner.history().size() - 1).residualNorm();
            history.add(new ComplexKleinGordonOuterRecord(cycle, binding, epsilon, next,
                    change, inner.history().size(), residual));
            monitor.progress((double) cycle / kg.maximumOuterIterations(),
                    "Complex Klein-Gordon outer cycle " + cycle);
            binding = next;
            if (inner.converged() && change <= kg.outerToleranceMeV()) {
                converged = true;
                break;
            }
        }
        BopitProblem referenceProblem = new BopitProblem(problem.atomicSystem(), problem.quantumState(),
                problem.waveEquation(), problem.grid(), problem.solver(), problem.electromagnetic(),
                new NoStrongInteractionSpec());
        double reference = new KleinGordonCalculator().calculate(referenceProblem, constants,
                CalculationMonitor.NONE).bindingEnergyMeV();
        double shift = binding.getReal() - reference;
        double width = -2.0 * binding.getImaginary();
        var waves = ComplexWavefunctionFactory.create(problem.quantumState().orbitalL(),
                grid, inner.eigenvector());
        return new ComplexKleinGordonResult(converged, binding, reference, shift, width,
                grid, history, inner, waves,
                converged ? "outer and inner convergence" : "maximum outer cycles reached");
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
