package edu.cnu.bopit.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiracCoulombEnergyTest {
    @Test
    void tendsToSchrodingerEnergyAtSmallCoupling() {
        double mu = 485.7;
        double alpha = 1e-5;
        double dirac = DiracCoulombEnergy.bindingEnergyMeV(mu, 2, alpha, 3, -3);
        double schrodinger = -0.5 * mu * Math.pow(2.0 * alpha / 3.0, 2);
        assertEquals(schrodinger, dirac, Math.abs(schrodinger) * 2e-6);
    }
}
