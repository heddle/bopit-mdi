package edu.cnu.bopit.study;

/** Axis identity and selected value for one study point. */
public record StudyCoordinate(String axisName, ParameterTarget target, int index,
        AxisValue value) {
    public StudyCoordinate {
        if (axisName == null || axisName.isBlank() || target == null || index < 0 || value == null) {
            throw new IllegalArgumentException("invalid study coordinate");
        }
    }
}
