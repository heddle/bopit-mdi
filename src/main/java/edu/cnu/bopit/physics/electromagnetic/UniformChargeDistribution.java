package edu.cnu.bopit.physics.electromagnetic;

/** Uniform sphere whose sharp radius is sqrt(5/3) times its rms radius. */
public final class UniformChargeDistribution implements ChargeDistribution {
    private final double rmsRadiusFm;
    private final double sharpRadiusFm;
    private final double density;

    public UniformChargeDistribution(double rmsRadiusFm) {
        requirePositive(rmsRadiusFm);
        this.rmsRadiusFm = rmsRadiusFm;
        sharpRadiusFm = Math.sqrt(5.0 / 3.0) * rmsRadiusFm;
        density = 3.0 / (4.0 * Math.PI * sharpRadiusFm * sharpRadiusFm * sharpRadiusFm);
    }

    @Override public double densityFmMinus3(double radiusFm) {
        requireRadius(radiusFm);
        return radiusFm <= sharpRadiusFm ? density : 0.0;
    }

    @Override public double formFactor(double q) {
        requireMomentum(q);
        double x = q * sharpRadiusFm;
        if (Math.abs(x) < 1e-3) {
            double x2 = x * x;
            return 1.0 - x2 / 10.0 + x2 * x2 / 280.0 - x2 * x2 * x2 / 15120.0;
        }
        return 3.0 * (Math.sin(x) - x * Math.cos(x)) / (x * x * x);
    }

    @Override public double rmsRadiusFm() { return rmsRadiusFm; }

    private static void requirePositive(double value) {
        if (!Double.isFinite(value) || value <= 0.0) throw new IllegalArgumentException("rms radius must be positive");
    }
    private static void requireRadius(double r) {
        if (!Double.isFinite(r) || r < 0.0) throw new IllegalArgumentException("radius must be nonnegative");
    }
    private static void requireMomentum(double q) { requireRadius(q); }
}
