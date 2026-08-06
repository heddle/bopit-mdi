package edu.cnu.bopit.physics.special;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.BrentSolver;

/** Gegenbauer polynomials C_degree^lambda(x) and their roots on (-1, 1). */
public final class Gegenbauer {
    private Gegenbauer() {
    }

    public static double value(int degree, double lambda, double x) {
        if (degree < 0 || !Double.isFinite(lambda) || lambda <= -0.5
                || !Double.isFinite(x)) {
            throw new IllegalArgumentException("invalid Gegenbauer input");
        }
        if (degree == 0) return 1.0;
        double previous = 1.0;
        double current = 2.0 * lambda * x;
        if (degree == 1) return current;
        for (int n = 2; n <= degree; n++) {
            double next = (2.0 * (n + lambda - 1.0) * x * current
                    - (n + 2.0 * lambda - 2.0) * previous) / n;
            previous = current;
            current = next;
        }
        return current;
    }

    /** Returns roots in strictly ascending order. */
    public static double[] zeros(int degree, double lambda) {
        if (degree < 0) throw new IllegalArgumentException("degree must be nonnegative");
        if (degree == 0) return new double[0];
        int subdivisions = Math.max(2_000, degree * 500);
        List<Double> roots = new ArrayList<>(degree);
        BrentSolver solver = new BrentSolver(1e-14, 1e-14);
        double left = -1.0;
        double fLeft = value(degree, lambda, left);
        for (int i = 1; i <= subdivisions && roots.size() < degree; i++) {
            double right = -1.0 + 2.0 * i / subdivisions;
            double fRight = value(degree, lambda, right);
            if (fRight == 0.0) {
                addDistinct(roots, right);
            } else if (Math.copySign(1.0, fLeft) != Math.copySign(1.0, fRight)) {
                addDistinct(roots, solver.solve(200, x -> value(degree, lambda, x), left, right));
            }
            left = right;
            fLeft = fRight;
        }
        if (roots.size() != degree) {
            throw new ArithmeticException("found " + roots.size() + " of " + degree + " Gegenbauer roots");
        }
        double[] result = new double[degree];
        for (int i = 0; i < degree; i++) result[i] = roots.get(i);
        return result;
    }

    private static void addDistinct(List<Double> roots, double root) {
        if (roots.isEmpty() || Math.abs(root - roots.get(roots.size() - 1)) > 1e-12) {
            roots.add(root);
        }
    }
}
