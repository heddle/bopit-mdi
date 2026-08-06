package edu.cnu.bopit.physics.coulomb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PointCoulombKernelTest {
    private final PointCoulombKernel kernel = new PointCoulombKernel(16, 7.29735e-3, 197.32858);

    @Test
    void isAttractiveSymmetricAndOffDiagonalOnly() {
        double forward = kernel.offDiagonal(2, 0.05, 0.2);
        assertTrue(forward < 0.0);
        assertEquals(forward, kernel.offDiagonal(2, 0.2, 0.05), Math.abs(forward) * 1e-14);
        assertThrows(IllegalArgumentException.class, () -> kernel.offDiagonal(2, 0.1, 0.1));
    }
}
