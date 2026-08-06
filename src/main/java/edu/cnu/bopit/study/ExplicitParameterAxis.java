package edu.cnu.bopit.study;

import java.util.List;

/** Explicit numeric values, optionally carrying categorical display labels. */
public record ExplicitParameterAxis(String name, ParameterTarget target,
        List<AxisValue> values) implements ParameterAxis {
    public ExplicitParameterAxis {
        if (name == null || name.isBlank() || target == null || values == null || values.isEmpty()) {
            throw new IllegalArgumentException("invalid explicit parameter axis");
        }
        values = List.copyOf(values);
    }
}
