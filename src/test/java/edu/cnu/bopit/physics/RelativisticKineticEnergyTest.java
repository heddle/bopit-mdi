package edu.cnu.bopit.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelativisticKineticEnergyTest {
    @Test
    void agreesWithExactFormulaAndNonrelativisticLimit() {
        double p = 0.2;
        double hbarC = 197.32858;
        double particle = 493.667;
        double nucleus = 30_032.0;
        double pc = p * hbarC;
        double expected = Math.sqrt(pc * pc + particle * particle) - particle
                + Math.sqrt(pc * pc + nucleus * nucleus) - nucleus;
        double actual = RelativisticKineticEnergy.twoBodyMeV(p, particle, nucleus, hbarC);
        assertEquals(expected, actual, 3e-12);
        double mu = ReducedMass.of(particle, nucleus);
        double nonrelativistic = pc * pc / (2.0 * mu);
        assertTrue(actual < nonrelativistic);
        assertEquals(nonrelativistic, actual, 0.003 * nonrelativistic);
    }

    @Test
    void remainsStableAtVerySmallMomentum() {
        double actual = RelativisticKineticEnergy.twoBodyMeV(
                1e-10, 493.667, 30_032.0, 197.32858);
        assertTrue(actual > 0.0);
        assertTrue(Double.isFinite(actual));
    }
}
