package edu.cnu.bopit.physics.coulomb;

/** Analytic constants and subtraction integral used by Landé regularization. */
public final class LandeIntegral {
    private static final double[] I = {
        0.0, 1.0, 1.2247449, 1.3228549, 1.3777022, 1.4127050,
        1.4369752, 1.4547898, 1.4684210, 1.4791869, 1.4879047
    };

    private LandeIntegral() {
    }

    public static double constant(int orbitalL) {
        if (orbitalL < 0 || orbitalL >= I.length) {
            throw new IllegalArgumentException("BOPIT Stage 1 supports Landé constants for 0 <= l <= 10");
        }
        return orbitalL == 2 ? Math.sqrt(1.5) : I[orbitalL];
    }

    public static double valueMeVFm(int orbitalL, double momentum,
            int nuclearCharge, double alpha, double hbarCMeVFm) {
        if (!Double.isFinite(momentum) || momentum <= 0.0) {
            throw new IllegalArgumentException("momentum must be finite and positive");
        }
        return -nuclearCharge * alpha * hbarCMeVFm
                * (Math.PI / 2.0 - constant(orbitalL)) / momentum;
    }
}
