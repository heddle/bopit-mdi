package edu.cnu.bopit.study;

import java.util.ArrayList;
import java.util.List;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;

/** Sequential headless execution of one- and two-axis parameter studies. */
public final class ParameterStudyRunner {
    public ParameterStudyResult run(ParameterStudy study, PhysicalConstantSet constants,
            CalculationMonitor monitor) {
        if (study == null || constants == null) throw new IllegalArgumentException("study and constants are required");
        monitor = monitor == null ? CalculationMonitor.NONE : monitor;
        List<List<StudyCoordinate>> coordinates = coordinates(study.axes());
        List<StudyPointResult> points = new ArrayList<>(coordinates.size());
        for (int index = 0; index < coordinates.size(); index++) {
            if (monitor.isCancellationRequested()) {
                return new ParameterStudyResult(study, points, true);
            }
            List<StudyCoordinate> coordinate = coordinates.get(index);
            final int pointIndex = index;
            final CalculationMonitor outer = monitor;
            try {
                BopitProblem varied = apply(study.baseProblem(), coordinate);
                var outcome = new StudyPointCalculator().calculate(varied, constants,
                        new CalculationMonitor() {
                            @Override public boolean isCancellationRequested() {
                                return outer.isCancellationRequested();
                            }
                            @Override public void progress(double fraction, String message) {
                                outer.progress((pointIndex + fraction) / coordinates.size(), message);
                            }
                        });
                points.add(new StudyPointResult(coordinate,
                        outcome.converged() ? StudyPointStatus.SUCCESS : StudyPointStatus.NON_CONVERGED,
                        outcome.observables(), outcome.message()));
            } catch (edu.cnu.bopit.calculation.CalculationCancelledException cancelled) {
                return new ParameterStudyResult(study, points, true);
            } catch (RuntimeException failure) {
                points.add(new StudyPointResult(coordinate, StudyPointStatus.FAILED,
                        java.util.Map.of(), failure.getClass().getSimpleName() + ": " + failure.getMessage()));
            }
            monitor.progress((index + 1.0) / coordinates.size(),
                    "Study point " + (index + 1) + " of " + coordinates.size());
        }
        return new ParameterStudyResult(study, points, false);
    }

    public static BopitProblem apply(BopitProblem base, List<StudyCoordinate> coordinates) {
        BopitProblem result = base;
        for (StudyCoordinate coordinate : coordinates) {
            result = coordinate.target().apply(result, coordinate.value().value());
        }
        return result;
    }

    public static List<List<StudyCoordinate>> coordinates(List<ParameterAxis> axes) {
        List<List<StudyCoordinate>> result = new ArrayList<>();
        ParameterAxis first = axes.get(0);
        for (int i = 0; i < first.values().size(); i++) {
            StudyCoordinate a = new StudyCoordinate(first.name(), first.target(), i, first.values().get(i));
            if (axes.size() == 1) result.add(List.of(a));
            else {
                ParameterAxis second = axes.get(1);
                for (int j = 0; j < second.values().size(); j++) {
                    StudyCoordinate b = new StudyCoordinate(second.name(), second.target(), j,
                            second.values().get(j));
                    result.add(List.of(a, b));
                }
            }
        }
        return List.copyOf(result);
    }
}
