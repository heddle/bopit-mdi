package edu.cnu.bopit.study;

import java.util.List;

import edu.cnu.bopit.model.BopitProblem;

/** Immutable one- or two-axis sequential study definition. */
public record ParameterStudy(String name, BopitProblem baseProblem,
        List<ParameterAxis> axes, List<StudyObservable> observables) {
    public ParameterStudy {
        if (name == null || name.isBlank() || baseProblem == null || axes == null
                || axes.isEmpty() || axes.size() > 2 || observables == null || observables.isEmpty()) {
            throw new IllegalArgumentException("study requires a name, problem, 1-2 axes, and observables");
        }
        axes = List.copyOf(axes);
        observables = List.copyOf(observables);
        int pointCount = axes.stream().mapToInt(axis -> axis.values().size()).reduce(1, Math::multiplyExact);
        if (pointCount > 10_000) throw new IllegalArgumentException("study exceeds 10,000 points");
    }
    public int pointCount() {
        return axes.stream().mapToInt(axis -> axis.values().size()).reduce(1, Math::multiplyExact);
    }
}
