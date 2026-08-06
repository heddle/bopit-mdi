package edu.cnu.bopit.study;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Inclusive logarithmically spaced positive parameter values. */
public record LogarithmicParameterAxis(String name, ParameterTarget target,
        double minimum, double maximum, int count) implements ParameterAxis {
    public LogarithmicParameterAxis {
        if (name == null || name.isBlank() || target == null || !Double.isFinite(minimum)
                || !Double.isFinite(maximum) || minimum <= 0.0 || maximum < minimum || count < 1) {
            throw new IllegalArgumentException("invalid logarithmic parameter axis");
        }
    }
    @Override public List<AxisValue> values() {
        List<AxisValue> values = new ArrayList<>(count);
        double logMin = Math.log(minimum);
        double logMax = Math.log(maximum);
        for (int i = 0; i < count; i++) {
            double value = count == 1 ? minimum
                    : Math.exp(logMin + (logMax - logMin) * i / (count - 1.0));
            values.add(new AxisValue(String.format(Locale.US, "%.9g", value), value));
        }
        return List.copyOf(values);
    }
}
