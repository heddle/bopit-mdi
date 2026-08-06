package edu.cnu.bopit.physics.special;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AssociatedLaguerreTest {
    @Test
    void matchesFirstPolynomials() {
        assertEquals(1.0, AssociatedLaguerre.value(0, 3.0, 2.0), 0.0);
        assertEquals(2.0, AssociatedLaguerre.value(1, 3.0, 2.0), 0.0);
        assertEquals(-1.0, AssociatedLaguerre.value(2, 1.0, 2.0), 1e-15);
    }
}
