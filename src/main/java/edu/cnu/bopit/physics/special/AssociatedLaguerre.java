package edu.cnu.bopit.physics.special;

/** Generalized Laguerre polynomials L_degree^alpha(x). */
public final class AssociatedLaguerre {
    private AssociatedLaguerre() { }

    public static double value(int degree, double alpha, double x) {
        if (degree < 0 || !Double.isFinite(alpha) || alpha <= -1.0 || !Double.isFinite(x)) {
            throw new IllegalArgumentException("invalid generalized Laguerre input");
        }
        if (degree == 0) return 1.0;
        double previous = 1.0;
        double current = 1.0 + alpha - x;
        if (degree == 1) return current;
        for (int n = 1; n < degree; n++) {
            double next = ((2.0 * n + 1.0 + alpha - x) * current
                    - (n + alpha) * previous) / (n + 1.0);
            previous = current;
            current = next;
        }
        return current;
    }
}
