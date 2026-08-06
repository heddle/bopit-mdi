package edu.cnu.bopit.physics.special;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GegenbauerTest {
    @Test
    void evaluatesKnownLowDegreeForms() {
        double lambda = 3.0;
        double x = 0.27;
        assertEquals(1.0, Gegenbauer.value(0, lambda, x));
        assertEquals(2.0 * lambda * x, Gegenbauer.value(1, lambda, x), 1e-15);
        assertEquals(2.0 * lambda * (lambda + 1.0) * x * x - lambda,
                Gegenbauer.value(2, lambda, x), 1e-15);
    }

    @Test
    void findsOrderedSymmetricZeros() {
        double[] roots = Gegenbauer.zeros(4, 2.0);
        assertEquals(4, roots.length);
        for (int i = 0; i < roots.length; i++) {
            assertEquals(0.0, Gegenbauer.value(4, 2.0, roots[i]), 2e-12);
            if (i > 0) assertTrue(roots[i] > roots[i - 1]);
            assertEquals(-roots[i], roots[roots.length - 1 - i], 2e-13);
        }
    }

    @Test
    void reproducesAnalyticQuadraticZeros() {
        double lambda = 3.0;
        double[] roots = Gegenbauer.zeros(2, lambda);
        double expected = 1.0 / Math.sqrt(2.0 * (lambda + 1.0));
        assertEquals(-expected, roots[0], 2e-14);
        assertEquals(expected, roots[1], 2e-14);
    }
}
