package edu.cnu.bopit.physics.special;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SphericalBesselTest {
    @Test
    void matchesAnalyticLowOrdersAndOriginLimits() {
        assertEquals(1.0, SphericalBessel.value(0, 0.0), 0.0);
        assertEquals(0.0, SphericalBessel.value(2, 0.0), 0.0);
        double x = 2.3;
        assertEquals(Math.sin(x) / x, SphericalBessel.value(0, x), 1e-15);
        assertEquals(Math.sin(x) / (x * x) - Math.cos(x) / x,
                SphericalBessel.value(1, x), 1e-15);
        double j2 = (3.0 / (x * x * x) - 1.0 / x) * Math.sin(x)
                - 3.0 * Math.cos(x) / (x * x);
        assertEquals(j2, SphericalBessel.value(2, x), 1e-15);
    }

    @Test
    void smallArgumentSeriesHasExpectedLeadingTerm() {
        double x = 1e-5;
        assertEquals(x * x / 15.0, SphericalBessel.value(2, x), 1e-22);
    }
}
