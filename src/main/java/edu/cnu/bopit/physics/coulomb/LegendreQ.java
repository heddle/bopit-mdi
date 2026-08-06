package edu.cnu.bopit.physics.coulomb;

import org.apache.commons.math3.special.Gamma;

/** Real Legendre functions of the second kind for integer order and z > 1. */
public final class LegendreQ {
    private LegendreQ() {
    }

    public static double value(int order, double z) {
        if (order < 0 || !Double.isFinite(z) || z <= 1.0) {
            throw new IllegalArgumentException("Q_l requires integer l >= 0 and finite z > 1");
        }
        if (z > 4.0) return asymptoticSeries(order, z);
        double q0 = 0.5 * Math.log1p(2.0 / (z - 1.0));
        if (order == 0) return q0;
        double q1 = z * q0 - 1.0;
        if (order == 1) return q1;
        double previous = q0;
        double current = q1;
        for (int l = 1; l < order; l++) {
            double next = ((2.0 * l + 1.0) * z * current - l * previous) / (l + 1.0);
            previous = current;
            current = next;
        }
        return current;
    }

    // DLMF 14.3.7 specialized to integer order. This avoids catastrophic
    // cancellation in the elementary recurrence at widely separated momenta.
    private static double asymptoticSeries(int order, double z) {
        double a = 0.5 * (order + 1.0);
        double b = 0.5 * (order + 2.0);
        double c = order + 1.5;
        double x = 1.0 / (z * z);
        double sum = 1.0;
        double term = 1.0;
        for (int k = 0; k < 10_000; k++) {
            term *= (a + k) * (b + k) * x / ((c + k) * (k + 1.0));
            sum += term;
            if (Math.abs(term) <= Math.ulp(sum)) break;
        }
        double logCoefficient = 0.5 * Math.log(Math.PI) + Gamma.logGamma(order + 1.0)
                - Gamma.logGamma(order + 1.5) - (order + 1.0) * Math.log(2.0 * z);
        return Math.exp(logCoefficient) * sum;
    }
}
