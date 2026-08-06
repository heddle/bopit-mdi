package edu.cnu.bopit.study;

import java.util.List;

/** One finite parameter-study axis. */
public sealed interface ParameterAxis permits LinearParameterAxis,
        LogarithmicParameterAxis, ExplicitParameterAxis {
    String name();
    ParameterTarget target();
    List<AxisValue> values();
}
