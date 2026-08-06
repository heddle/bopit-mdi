package edu.cnu.bopit.physics.electromagnetic;

import edu.cnu.bopit.grid.GaussLegendreRule;
import edu.cnu.bopit.grid.GaussLegendreRules;

/** Numerically normalized three-parameter Fermi charge distribution. */
public final class FermiChargeDistribution implements ChargeDistribution {
    private static final int RADIAL_ORDER = 192;
    private final double c;
    private final double a;
    private final double w;
    private final double normalization;
    private final double rmsRadius;
    private final double[] radii;
    private final double[] radialWeights;
    private final double[] normalizedRadialCharge;

    public FermiChargeDistribution(double halfDensityRadiusFm, double diffusenessFm, double w) {
        if (!Double.isFinite(halfDensityRadiusFm) || halfDensityRadiusFm <= 0.0
                || !Double.isFinite(diffusenessFm) || diffusenessFm <= 0.0
                || !Double.isFinite(w)) {
            throw new IllegalArgumentException("invalid Fermi parameters");
        }
        c = halfDensityRadiusFm;
        a = diffusenessFm;
        this.w = w;
        double maximumRadius = c + 40.0 * a;
        GaussLegendreRule rule = new GaussLegendreRules().create(RADIAL_ORDER);
        radii = new double[RADIAL_ORDER];
        radialWeights = new double[RADIAL_ORDER];
        double integral = 0.0;
        double moment2 = 0.0;
        for (int i = 0; i < RADIAL_ORDER; i++) {
            double r = 0.5 * maximumRadius * (rule.node(i) + 1.0);
            double weight = 0.5 * maximumRadius * rule.weight(i);
            radii[i] = r;
            radialWeights[i] = weight;
            double shell = 4.0 * Math.PI * r * r * shape(r);
            integral += weight * shell;
            moment2 += weight * shell * r * r;
        }
        if (!(integral > 0.0) || !Double.isFinite(integral)) {
            throw new IllegalArgumentException("Fermi parameters do not define a positive charge normalization");
        }
        normalization = 1.0 / integral;
        rmsRadius = Math.sqrt(moment2 / integral);
        normalizedRadialCharge = new double[RADIAL_ORDER];
        for (int i = 0; i < RADIAL_ORDER; i++) {
            double r = radii[i];
            normalizedRadialCharge[i] = normalization * 4.0 * Math.PI * r * r * shape(r);
        }
    }

    @Override public double densityFmMinus3(double radiusFm) {
        requireNonnegative(radiusFm);
        return normalization * shape(radiusFm);
    }

    @Override public double formFactor(double q) {
        requireNonnegative(q);
        if (q == 0.0) return 1.0;
        double sum = 0.0;
        for (int i = 0; i < radii.length; i++) {
            double x = q * radii[i];
            double j0 = Math.abs(x) < 1e-5 ? 1.0 - x * x / 6.0 : Math.sin(x) / x;
            sum += radialWeights[i] * normalizedRadialCharge[i] * j0;
        }
        return sum;
    }

    @Override public double rmsRadiusFm() { return rmsRadius; }

    private double shape(double r) {
        double numerator = 1.0 + w * r * r / (c * c);
        double exponent = (r - c) / a;
        if (exponent > 50.0) return numerator * Math.exp(-exponent);
        return numerator / (1.0 + Math.exp(exponent));
    }

    private static void requireNonnegative(double value) {
        if (!Double.isFinite(value) || value < 0.0) throw new IllegalArgumentException("argument must be finite and nonnegative");
    }
}
