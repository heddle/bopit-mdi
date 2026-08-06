package edu.cnu.bopit.model;

/** Historical two-region BOPIT momentum-grid parameters, all in fm^-1. */
public record LegacyGridSpec(int totalPoints, int nuclearPoints,
        double atomicScale, double nuclearScale, double regionBoundary,
        double maximumMomentumScale) {
    public LegacyGridSpec {
        if (totalPoints < 4) throw new IllegalArgumentException("totalPoints must be >= 4");
        if (nuclearPoints <= 0 || nuclearPoints >= totalPoints) {
            throw new IllegalArgumentException("nuclearPoints must be between zero and totalPoints");
        }
        if (((totalPoints - nuclearPoints) & 1) != 0 || (nuclearPoints & 1) != 0) {
            throw new IllegalArgumentException("both legacy-grid region counts must be even");
        }
        requirePositive(atomicScale, "atomicScale");
        requirePositive(nuclearScale, "nuclearScale");
        requirePositive(regionBoundary, "regionBoundary");
        requirePositive(maximumMomentumScale, "maximumMomentumScale");
    }

    public int atomicPoints() {
        return totalPoints - nuclearPoints;
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }
}
