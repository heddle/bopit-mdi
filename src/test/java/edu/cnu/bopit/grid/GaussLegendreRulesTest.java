package edu.cnu.bopit.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GaussLegendreRulesTest {
    @Test
    void integratesPolynomialsThroughExpectedDegree() {
        GaussLegendreRule rule = new GaussLegendreRules().create(6);
        for (int power = 0; power <= 11; power++) {
            double sum = 0.0;
            for (int i = 0; i < rule.size(); i++) {
                assertTrue(rule.weight(i) > 0.0);
                sum += rule.weight(i) * Math.pow(rule.node(i), power);
            }
            double exact = power % 2 == 0 ? 2.0 / (power + 1) : 0.0;
            assertEquals(exact, sum, 2e-14);
        }
    }
}
