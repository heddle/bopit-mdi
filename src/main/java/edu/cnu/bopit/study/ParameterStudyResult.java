package edu.cnu.bopit.study;

import java.util.List;

/** Completed or cancelled sequential study, retaining every attempted point. */
public record ParameterStudyResult(ParameterStudy study, List<StudyPointResult> points,
        boolean cancelled) {
    public ParameterStudyResult {
        if (study == null || points == null) throw new IllegalArgumentException("study result fields are required");
        points = List.copyOf(points);
    }
}
