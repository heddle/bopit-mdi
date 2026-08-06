package edu.cnu.bopit.physics.electromagnetic;

/** Simple Gaussian density with form factor exp(-q^2 rms^2/6). */
public final class GaussianChargeDistribution implements ChargeDistribution {
    private final double rmsRadiusFm;
    private final double exponentCoefficient;
    private final double densityAtOrigin;

    public GaussianChargeDistribution(double rmsRadiusFm) {
        if (!Double.isFinite(rmsRadiusFm) || rmsRadiusFm <= 0.0) {
            throw new IllegalArgumentException("rms radius must be finite and positive");
        }
        this.rmsRadiusFm = rmsRadiusFm;
        exponentCoefficient = 3.0 / (2.0 * rmsRadiusFm * rmsRadiusFm);
        densityAtOrigin = Math.pow(exponentCoefficient / Math.PI, 1.5);
    }

    @Override public double densityFmMinus3(double r) {
        requireNonnegative(r);
        return densityAtOrigin * Math.exp(-exponentCoefficient * r * r);
    }

    @Override public double formFactor(double q) {
        requireNonnegative(q);
        return Math.exp(-q * q * rmsRadiusFm * rmsRadiusFm / 6.0);
    }

    @Override public double rmsRadiusFm() { return rmsRadiusFm; }

    private static void requireNonnegative(double value) {
        if (!Double.isFinite(value) || value < 0.0) throw new IllegalArgumentException("argument must be finite and nonnegative");
    }
}
