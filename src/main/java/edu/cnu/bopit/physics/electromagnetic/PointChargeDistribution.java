package edu.cnu.bopit.physics.electromagnetic;

/** Unit point charge. */
public final class PointChargeDistribution implements ChargeDistribution {
    @Override public double densityFmMinus3(double radiusFm) {
        throw new UnsupportedOperationException("a point charge has a distributional density");
    }

    @Override public double formFactor(double momentumTransferFmInverse) {
        requireMomentum(momentumTransferFmInverse);
        return 1.0;
    }

    @Override public double rmsRadiusFm() { return 0.0; }
    @Override public boolean isPointCharge() { return true; }

    private static void requireMomentum(double q) {
        if (!Double.isFinite(q) || q < 0.0) {
            throw new IllegalArgumentException("momentum transfer must be finite and nonnegative");
        }
    }
}
