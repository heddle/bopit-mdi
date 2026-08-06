package edu.cnu.bopit.model;

/** Inputs to the published automatic Coulomb-wavefunction grid method. */
public record AdaptiveGridSpec(int totalPoints, int nuclearPoints,
        double maximumMomentum, double regionBias) implements GridSpec {
    public AdaptiveGridSpec {
        if (totalPoints < 4) {
            throw new IllegalArgumentException("totalPoints must be at least 4");
        }
        if (nuclearPoints <= 0 || nuclearPoints >= totalPoints) {
            throw new IllegalArgumentException("nuclearPoints must be between zero and totalPoints");
        }
        if ((nuclearPoints & 1) != 0) {
            throw new IllegalArgumentException("nuclearPoints must be even");
        }
        if (!Double.isFinite(maximumMomentum) || maximumMomentum <= 0.0) {
            throw new IllegalArgumentException("maximumMomentum must be finite and positive");
        }
        if (!Double.isFinite(regionBias) || regionBias <= 0.0 || regionBias >= 1.0) {
            throw new IllegalArgumentException("regionBias must lie strictly between zero and one");
        }
    }
}
