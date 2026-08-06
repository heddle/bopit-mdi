package edu.cnu.bopit.study;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParameterAxisTest {
    @Test
    void generatesLinearLogarithmicAndExplicitValues() {
        var linear = new LinearParameterAxis("x", ParameterTarget.CHARGE_RADIUS_FM, 2.0, 4.0, 3);
        assertEquals(java.util.List.of(2.0, 3.0, 4.0),
                linear.values().stream().map(AxisValue::value).toList());
        var logarithmic = new LogarithmicParameterAxis("x",
                ParameterTarget.MAXIMUM_MOMENTUM_FM_INVERSE, 1.0, 100.0, 3);
        assertEquals(10.0, logarithmic.values().get(1).value(), 1e-12);
        var explicit = new ExplicitParameterAxis("kind", ParameterTarget.GRID_KIND,
                java.util.List.of(new AxisValue("legacy", 0), new AxisValue("adaptive", 1)));
        assertEquals("adaptive", explicit.values().get(1).label());
    }
}
