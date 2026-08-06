package edu.cnu.bopit.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit schema DTO for a problem; variant fields use stable type names. */
public record ProblemDto(Map<String, Object> atom, Map<String, Object> state,
        Map<String, Object> waveEquation, Map<String, Object> grid,
        Map<String, Object> solver, Map<String, Object> electromagnetic,
        Map<String, Object> strongInteraction) {
    public ProblemDto {
        atom = copy(atom); state = copy(state); waveEquation = copy(waveEquation);
        grid = copy(grid); solver = copy(solver); electromagnetic = copy(electromagnetic);
        strongInteraction = copy(strongInteraction);
    }
    private static Map<String, Object> copy(Map<String, Object> value) {
        if (value == null) throw new IllegalArgumentException("problem DTO sections are required");
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}
