package edu.cnu.bopit.study;

/** One labeled numeric coordinate on a parameter axis. */
public record AxisValue(String label, double value) {
    public AxisValue {
        if (label == null || label.isBlank() || !Double.isFinite(value)) {
            throw new IllegalArgumentException("axis value requires a label and finite value");
        }
    }
}
