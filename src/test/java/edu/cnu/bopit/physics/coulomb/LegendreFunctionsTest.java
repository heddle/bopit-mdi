package edu.cnu.bopit.physics.coulomb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegendreFunctionsTest {
    @Test
    void evaluatesLowOrderPolynomials() {
        double x = 0.37;
        assertEquals(1.0, LegendreP.value(0, x));
        assertEquals(x, LegendreP.value(1, x));
        assertEquals(0.5 * (3.0 * x * x - 1.0), LegendreP.value(2, x), 1e-15);
    }

    @Test
    void evaluatesQAtModerateAndLargeArguments() {
        double z = 2.0;
        double q0 = 0.5 * Math.log(3.0);
        assertEquals(q0, LegendreQ.value(0, z), 1e-15);
        assertEquals(z * q0 - 1.0, LegendreQ.value(1, z), 1e-15);
        assertTrue(LegendreQ.value(2, 1.0e8) > 0.0);
        assertThrows(IllegalArgumentException.class, () -> LegendreQ.value(2, 1.0));
    }

    @Test
    void hasPublishedLandeConstants() {
        assertEquals(0.0, LandeIntegral.constant(0));
        assertEquals(1.0, LandeIntegral.constant(1));
        assertEquals(Math.sqrt(1.5), LandeIntegral.constant(2), 1e-15);
        assertEquals((8.0 + 5.0 * Math.sqrt(10.0)) / 18.0,
                LandeIntegral.constant(3), 5e-8);
    }
}
