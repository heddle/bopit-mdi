package edu.cnu.bopit.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KleinGordonCoulombEnergyTest {
    @Test
    void tendsToSchrodingerEnergyAtSmallCoupling() {
        double mu = 485.726;
        double alpha = 1e-5;
        double exact = KleinGordonCoulombEnergy.bindingEnergyMeV(mu, 2, alpha, 3, 2);
        double schrodinger = -0.5 * mu * Math.pow(2.0 * alpha / 3.0, 2);
        assertEquals(schrodinger, exact, Math.abs(schrodinger) * 2e-6);
    }
}
