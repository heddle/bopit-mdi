package edu.cnu.bopit.physics.coulomb;

/** Integer-order Legendre polynomials evaluated by three-term recurrence. */
public final class LegendreP {
    private LegendreP() {
    }

    public static double value(int order, double x) {
        if (order < 0 || !Double.isFinite(x)) throw new IllegalArgumentException("invalid P_l input");
        if (order == 0) return 1.0;
        if (order == 1) return x;
        double previous = 1.0;
        double current = x;
        for (int l = 1; l < order; l++) {
            double next = ((2.0 * l + 1.0) * x * current - l * previous) / (l + 1.0);
            previous = current;
            current = next;
        }
        return current;
    }
}
