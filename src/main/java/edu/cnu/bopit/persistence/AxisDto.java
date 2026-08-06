package edu.cnu.bopit.persistence;

import java.util.List;

/** Explicit schema DTO for linear, logarithmic, or explicit study axes. */
public record AxisDto(String type, String name, String target, Double minimum,
        Double maximum, Integer count, List<ValueDto> values) {
    public AxisDto { values = values == null ? null : List.copyOf(values); }
    public record ValueDto(String label, double value) { }
}
