package edu.cnu.bopit.physics.electromagnetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChargeDistributionTest {
    @Test
    void uniformSphereHasPublishedRadiusAndFormFactorConventions() {
        double rms = 2.56;
        var charge = new UniformChargeDistribution(rms);
        assertEquals(1.0, integrateCharge(charge, 10.0), 2e-4);
        assertEquals(rms * rms, integrateMoment2(charge, 10.0), 2e-3);
        assertEquals(1.0, charge.formFactor(0.0));
        assertEquals(-rms * rms / 6.0,
                (charge.formFactor(1e-4) - 1.0) / 1e-8, 2e-7);
    }

    @Test
    void gaussianUsesSimpleRmsFormFactorFromKwonAndTabakin() {
        double rms = 1.64;
        var charge = new GaussianChargeDistribution(rms);
        assertEquals(1.0, integrateCharge(charge, 12.0), 2e-8);
        assertEquals(rms * rms, integrateMoment2(charge, 12.0), 2e-7);
        assertEquals(Math.exp(-0.7 * 0.7 * rms * rms / 6.0), charge.formFactor(0.7), 1e-15);
    }

    @Test
    void fermiDensityAndFormFactorAreNumericallyNormalized() {
        var charge = new FermiChargeDistribution(3.26, 0.59, 0.0);
        assertEquals(1.0, integrateCharge(charge, 30.0), 2e-7);
        assertEquals(1.0, charge.formFactor(0.0));
        assertEquals(-charge.rmsRadiusFm() * charge.rmsRadiusFm() / 6.0,
                (charge.formFactor(1e-3) - 1.0) / 1e-6, 2e-5);
        assertTrue(charge.rmsRadiusFm() > 3.0);
    }

    private static double integrateCharge(ChargeDistribution charge, double maximum) {
        return integrate(charge, maximum, false);
    }

    private static double integrateMoment2(ChargeDistribution charge, double maximum) {
        return integrate(charge, maximum, true);
    }

    private static double integrate(ChargeDistribution charge, double maximum, boolean moment2) {
        int intervals = 200_000;
        double step = maximum / intervals;
        double sum = 0.0;
        for (int i = 0; i <= intervals; i++) {
            double r = i * step;
            double value = 4.0 * Math.PI * r * r * charge.densityFmMinus3(r);
            if (moment2) value *= r * r;
            sum += (i == 0 || i == intervals ? 0.5 : 1.0) * value;
        }
        return step * sum;
    }
}
