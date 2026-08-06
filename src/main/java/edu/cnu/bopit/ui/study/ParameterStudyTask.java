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
import edu.cnu.mdi.sim.task.BackgroundTask;
import edu.cnu.mdi.sim.task.TaskContext;

/** One-shot MDI task that evaluates a parameter study sequentially. */
public final class ParameterStudyTask implements BackgroundTask<ParameterStudyResult> {
    private final ParameterStudy study;
    private final PhysicalConstantSet constants;

    public ParameterStudyTask(ParameterStudy study, PhysicalConstantSet constants) {
        this.study = Objects.requireNonNull(study, "study");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    @Override
    public ParameterStudyResult execute(TaskContext context) {
        List<List<StudyCoordinate>> coordinates = ParameterStudyRunner.coordinates(study.axes());
        List<StudyPointResult> points = new ArrayList<>();
        context.postMessage("Study ready: " + coordinates.size() + " sequential points");

        for (int index = 0; index < coordinates.size(); index++) {
            List<StudyCoordinate> coordinate = coordinates.get(index);
            int current = index;
            try {
                var problem = ParameterStudyRunner.apply(study.baseProblem(), coordinate);
                var outcome = new StudyPointCalculator().calculate(problem, constants,
                        new CalculationMonitor() {
                            @Override public boolean isCancellationRequested() {
                                return context.isCancellationRequested();
                            }
                            @Override public void progress(double fraction, String message) {
                                context.reportProgress(
                                        (current + fraction) / coordinates.size(), message);
                            }
                        });
                points.add(new StudyPointResult(coordinate,
                        outcome.converged() ? StudyPointStatus.SUCCESS
                                : StudyPointStatus.NON_CONVERGED,
                        outcome.observables(), outcome.message()));
            } catch (CalculationCancelledException cancelled) {
                if (!context.isCancellationRequested()) throw cancelled;
                return new ParameterStudyResult(study, List.copyOf(points), true);
            } catch (RuntimeException failure) {
                points.add(new StudyPointResult(coordinate, StudyPointStatus.FAILED, Map.of(),
                        failure.getClass().getSimpleName() + ": " + failure.getMessage()));
            }
            int completed = index + 1;
            context.reportProgress((double) completed / coordinates.size(),
                    "Study point " + completed + " of " + coordinates.size());
            context.postMessage("Completed study point " + completed
                    + " of " + coordinates.size());
        }
        return new ParameterStudyResult(study, List.copyOf(points), false);
    }
}
