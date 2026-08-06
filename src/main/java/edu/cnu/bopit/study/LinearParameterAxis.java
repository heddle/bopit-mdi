package edu.cnu.bopit.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Inclusive linearly spaced parameter values. */
public record LinearParameterAxis(String name, ParameterTarget target,
        double minimum, double maximum, int count) implements ParameterAxis {
    public LinearParameterAxis {
        validate(name, target, minimum, maximum, count);
    }
    @Override public List<AxisValue> values() {
        List<AxisValue> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double value = count == 1 ? minimum : minimum + (maximum - minimum) * i / (count - 1.0);
            values.add(new AxisValue(String.format(Locale.US, "%.9g", value), value));
        }
        return List.copyOf(values);
    }
    private static void validate(String name, ParameterTarget target,
            double minimum, double maximum, int count) {
        if (name == null || name.isBlank() || target == null || !Double.isFinite(minimum)
                || !Double.isFinite(maximum) || maximum < minimum || count < 1) {
            throw new IllegalArgumentException("invalid linear parameter axis");
        }
    }
}
