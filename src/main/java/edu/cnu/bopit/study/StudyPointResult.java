package edu.cnu.bopit.study;

import java.util.List;
import java.util.Map;

/** Retained success, non-convergence, or failure at one parameter coordinate. */
public record StudyPointResult(List<StudyCoordinate> coordinates, StudyPointStatus status,
        Map<StudyObservable, Double> observables, String message) {
    public StudyPointResult {
        if (coordinates == null || coordinates.isEmpty() || coordinates.size() > 2
                || status == null || observables == null || message == null) {
            throw new IllegalArgumentException("invalid study point result");
        }
        coordinates = List.copyOf(coordinates);
        observables = Map.copyOf(observables);
    }
}
