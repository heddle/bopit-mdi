package edu.cnu.bopit.ui.workbench;

import java.util.Objects;
import java.util.Optional;

import edu.cnu.bopit.calculation.CalculationCancelledException;
import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.StrongInteractionCalculator;
import edu.cnu.bopit.calculation.StrongInteractionResult;
import edu.cnu.bopit.calculation.KleinGordonCalculator;
import edu.cnu.bopit.calculation.KleinGordonResult;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import org.apache.commons.math3.complex.Complex;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.mdi.sim.ProgressInfo;
import edu.cnu.mdi.sim.Simulation;
import edu.cnu.mdi.sim.SimulationContext;
import edu.cnu.mdi.sim.SimulationEngine;

/** MDI background-execution adapter for one immutable point-Coulomb problem. */
public final class BopitCalculationSimulation implements Simulation {
    private final BopitProblem problem;
    private final PhysicalConstantSet constants;
    private volatile SimulationEngine engine;
    private volatile PointCoulombResult result;
    private volatile StrongInteractionResult strongResult;
    private volatile KleinGordonResult kleinGordonResult;
    private boolean executed;

    public BopitCalculationSimulation(BopitProblem problem, PhysicalConstantSet constants) {
        this.problem = Objects.requireNonNull(problem, "problem");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    /** Bind the engine used to publish progress and messages. */
    public void bindEngine(SimulationEngine engine) {
        if (this.engine != null) throw new IllegalStateException("engine already bound");
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    /** Completed result, empty before successful termination or after cancellation. */
    public Optional<PointCoulombResult> result() {
        return Optional.ofNullable(result);
    }

    /** Completed complex result when a strong interaction was selected. */
    public Optional<StrongInteractionResult> strongResult() {
        return Optional.ofNullable(strongResult);
    }

    /** Completed Klein-Gordon result when that equation was selected. */
    public Optional<KleinGordonResult> kleinGordonResult() {
        return Optional.ofNullable(kleinGordonResult);
    }

    @Override
    public void init(SimulationContext context) {
        requireEngine().postMessage(problem.strongInteraction() instanceof NoStrongInteractionSpec
                ? "Electromagnetic calculation ready"
                : "Complex strong-interaction calculation ready");
    }

    @Override
    public boolean step(SimulationContext context) {
        if (executed) return false;
        executed = true;
        CalculationMonitor monitor = new CalculationMonitor() {
            @Override
            public boolean isCancellationRequested() {
                return context.isCancelRequested();
            }

            @Override
            public void progress(double fraction, String message) {
                SimulationEngine boundEngine = requireEngine();
                boundEngine.postProgress(ProgressInfo.determinate(fraction, message));
                boundEngine.postMessage(message);
            }
        };
        try {
            if (problem.waveEquation() instanceof KleinGordonSpec) {
                kleinGordonResult = new KleinGordonCalculator().calculate(problem, constants, monitor);
            } else if (problem.strongInteraction() instanceof NoStrongInteractionSpec) {
                result = new PointCoulombCalculator().calculate(problem, constants, monitor);
            } else {
                var realSpec = problem.solver();
                var complexSpec = new ComplexInverseIterationSpec(
                        new Complex(realSpec.shiftMeV(), -0.001),
                        realSpec.energyTolerance(), realSpec.residualTolerance(),
                        realSpec.minimumIterations(), realSpec.maximumIterations());
                strongResult = new StrongInteractionCalculator().calculate(
                        problem, constants, complexSpec, monitor);
            }
        } catch (CalculationCancelledException cancelled) {
            if (!context.isCancelRequested()) throw cancelled;
        }
        return false;
    }

    private SimulationEngine requireEngine() {
        SimulationEngine boundEngine = engine;
        if (boundEngine == null) throw new IllegalStateException("simulation engine is not bound");
        return boundEngine;
    }
}
