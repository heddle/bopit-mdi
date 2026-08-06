package edu.cnu.bopit.ui.workbench;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.calculation.CalculationCancelledException;
import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.ComplexKleinGordonCalculator;
import edu.cnu.bopit.calculation.DiracCalculator;
import edu.cnu.bopit.calculation.KleinGordonCalculator;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.StrongInteractionCalculator;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.mdi.sim.task.BackgroundTask;
import edu.cnu.mdi.sim.task.TaskContext;

/** One-shot MDI task for one immutable BOPIT problem. */
public final class BopitCalculationTask implements BackgroundTask<BopitCalculationOutcome> {
    private final BopitProblem problem;
    private final PhysicalConstantSet constants;

    public BopitCalculationTask(BopitProblem problem, PhysicalConstantSet constants) {
        this.problem = Objects.requireNonNull(problem, "problem");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    @Override
    public BopitCalculationOutcome execute(TaskContext context) {
        context.postMessage(problem.strongInteraction() instanceof NoStrongInteractionSpec
                ? "Electromagnetic calculation ready"
                : "Complex strong-interaction calculation ready");
        CalculationMonitor monitor = new CalculationMonitor() {
            @Override public boolean isCancellationRequested() {
                return context.isCancellationRequested();
            }
            @Override public void progress(double fraction, String message) {
                context.reportProgress(fraction, message);
                context.postMessage(message);
            }
        };
        try {
            if (problem.waveEquation() instanceof DiracSpec) {
                return new BopitCalculationOutcome.Dirac(
                        new DiracCalculator().calculate(problem, constants, monitor));
            }
            if (problem.waveEquation() instanceof KleinGordonSpec) {
                if (problem.strongInteraction() instanceof NoStrongInteractionSpec) {
                    return new BopitCalculationOutcome.KleinGordon(
                            new KleinGordonCalculator().calculate(problem, constants, monitor));
                }
                return new BopitCalculationOutcome.ComplexKleinGordon(
                        new ComplexKleinGordonCalculator().calculate(problem, constants, monitor));
            }
            if (problem.strongInteraction() instanceof NoStrongInteractionSpec) {
                return new BopitCalculationOutcome.PointCoulomb(
                        new PointCoulombCalculator().calculate(problem, constants, monitor));
            }
            var realSpec = problem.solver();
            var complexSpec = new ComplexInverseIterationSpec(
                    new Complex(realSpec.shiftMeV(), -0.001),
                    realSpec.energyTolerance(), realSpec.residualTolerance(),
                    realSpec.minimumIterations(), realSpec.maximumIterations());
            return new BopitCalculationOutcome.StrongInteraction(
                    new StrongInteractionCalculator().calculate(
                            problem, constants, complexSpec, monitor));
        } catch (CalculationCancelledException cancelled) {
            if (!context.isCancellationRequested()) throw cancelled;
            context.throwIfCancellationRequested();
            throw new AssertionError("unreachable");
        }
    }
}
