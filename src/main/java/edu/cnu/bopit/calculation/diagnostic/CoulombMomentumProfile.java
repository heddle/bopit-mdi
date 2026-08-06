package edu.cnu.bopit.calculation.diagnostic;

/** Normalized analytic point-Coulomb momentum profile used for grid diagnostics. */
public record CoulombMomentumProfile(double[] momentaFmInverse,
        double[] normalizedMagnitude) {
    public CoulombMomentumProfile {
        momentaFmInverse = momentaFmInverse.clone();
        normalizedMagnitude = normalizedMagnitude.clone();
        if (momentaFmInverse.length == 0
                || momentaFmInverse.length != normalizedMagnitude.length) {
            throw new IllegalArgumentException("profile arrays must have equal positive length");
        }
    }

    @Override public double[] momentaFmInverse() { return momentaFmInverse.clone(); }
    @Override public double[] normalizedMagnitude() { return normalizedMagnitude.clone(); }
}
