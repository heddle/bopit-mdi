package edu.cnu.bopit.persistence;

import java.util.List;

/** Explicit schema DTO for a reproducible one- or two-axis study definition. */
public record StudyDto(String name, ProblemDto baseProblem, List<AxisDto> axes,
        List<String> observables) {
    public StudyDto {
        axes = List.copyOf(axes);
        observables = List.copyOf(observables);
    }
}
