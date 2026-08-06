package edu.cnu.bopit.ui.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.cnu.bopit.calculation.CalculationCancelledException;
import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.study.ParameterStudyRunner;
import edu.cnu.bopit.study.StudyCoordinate;
import edu.cnu.bopit.study.StudyPointCalculator;
import edu.cnu.bopit.study.StudyPointResult;
import edu.cnu.bopit.study.StudyPointStatus;
import edu.cnu.mdi.sim.ProgressInfo;
import edu.cnu.mdi.sim.Simulation;
import edu.cnu.mdi.sim.SimulationContext;
import edu.cnu.mdi.sim.SimulationEngine;

/** One-complete-calculation-per-step MDI adapter for sequential studies. */
public final class ParameterStudySimulation implements Simulation {
    private final ParameterStudy study;
    private final PhysicalConstantSet constants;
    private final List<List<StudyCoordinate>> coordinates;
    private final List<StudyPointResult> points = new ArrayList<>();
    private volatile SimulationEngine engine;
    private int index;

    public ParameterStudySimulation(ParameterStudy study, PhysicalConstantSet constants) {
        this.study = Objects.requireNonNull(study, "study");
        this.constants = Objects.requireNonNull(constants, "constants");
        coordinates = ParameterStudyRunner.coordinates(study.axes());
    }

    public void bindEngine(SimulationEngine engine) {
        if (this.engine != null) throw new IllegalStateException("engine already bound");
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    public ParameterStudyResult result(boolean cancelled) {
        return new ParameterStudyResult(study, List.copyOf(points), cancelled);
    }

    @Override public void init(SimulationContext context) {
        requireEngine().postMessage("Study ready: " + coordinates.size() + " sequential points");
    }

    @Override public boolean step(SimulationContext context) {
        if (index >= coordinates.size()) return false;
        List<StudyCoordinate> coordinate = coordinates.get(index);
        int current = index;
        try {
            var problem = ParameterStudyRunner.apply(study.baseProblem(), coordinate);
            var outcome = new StudyPointCalculator().calculate(problem, constants,
                    new CalculationMonitor() {
                        @Override public boolean isCancellationRequested() {
                            return context.isCancelRequested();
                        }
                        @Override public void progress(double fraction, String message) {
                            requireEngine().postProgress(ProgressInfo.determinate(
                                    (current + fraction) / coordinates.size(), message));
                        }
                    });
            points.add(new StudyPointResult(coordinate,
                    outcome.converged() ? StudyPointStatus.SUCCESS : StudyPointStatus.NON_CONVERGED,
                    outcome.observables(), outcome.message()));
        } catch (CalculationCancelledException cancelled) {
            return false;
        } catch (RuntimeException failure) {
            points.add(new StudyPointResult(coordinate, StudyPointStatus.FAILED, Map.of(),
                    failure.getClass().getSimpleName() + ": " + failure.getMessage()));
        }
        index++;
        requireEngine().postProgress(ProgressInfo.determinate((double) index / coordinates.size(),
                "Study point " + index + " of " + coordinates.size()));
        requireEngine().postMessage("Completed study point " + index + " of " + coordinates.size());
        return index < coordinates.size();
    }

    private SimulationEngine requireEngine() {
        if (engine == null) throw new IllegalStateException("simulation engine is not bound");
        return engine;
    }
}
