package edu.cnu.bopit.physics.special;

/** Spherical Bessel functions j_l(x) for nonnegative integer order. */
public final class SphericalBessel {
    private SphericalBessel() { }

    /**
     * Evaluate j_l(x). A power series avoids cancellation near zero; elsewhere
     * the standard upward recurrence starts from analytic j_0 and j_1.
     */
    public static double value(int order, double x) {
        if (order < 0 || !Double.isFinite(x)) {
            throw new IllegalArgumentException("order must be nonnegative and x finite");
        }
        if (x == 0.0) return order == 0 ? 1.0 : 0.0;
        if (Math.abs(x) < 1.0) return series(order, x);
        double j0 = Math.sin(x) / x;
        if (order == 0) return j0;
        double j1 = Math.sin(x) / (x * x) - Math.cos(x) / x;
        if (order == 1) return j1;
        double previous = j0;
        double current = j1;
        for (int l = 1; l < order; l++) {
            double next = (2.0 * l + 1.0) * current / x - previous;
            previous = current;
            current = next;
        }
        return current;
    }

    private static double series(int order, double x) {
        double oddDoubleFactorial = 1.0;
        for (int factor = 1; factor <= 2 * order + 1; factor += 2) {
            oddDoubleFactorial *= factor;
        }
        double term = Math.pow(x, order) / oddDoubleFactorial;
        double sum = term;
        for (int k = 0; k < 100; k++) {
            term *= -x * x / (2.0 * (k + 1.0) * (2.0 * order + 2.0 * k + 3.0));
            sum += term;
            if (Math.abs(term) <= 2e-16 * Math.max(1.0, Math.abs(sum))) break;
        }
        return sum;
    }
}
