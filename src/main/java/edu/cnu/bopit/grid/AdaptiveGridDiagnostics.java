package edu.cnu.bopit.grid;

import java.util.List;

/** Nodes, boundaries, allocation, and adjustments from automatic grid construction. */
public record AdaptiveGridDiagnostics(double coulombMomentumScale,
        double atomicMomentumScale, double minimumMomentum,
        double nuclearRegionStart, double maximumMomentum,
        double[] atomicNodes, double[] regionBoundaries,
        int[] pointsPerRegion, double[] regionBiases, int requestedTotalPoints,
        int actualTotalPoints, List<String> warnings) implements GridDiagnostics {
    public AdaptiveGridDiagnostics {
        atomicNodes = atomicNodes.clone();
        regionBoundaries = regionBoundaries.clone();
        pointsPerRegion = pointsPerRegion.clone();
        regionBiases = regionBiases.clone();
        warnings = List.copyOf(warnings);
    }
    @Override public double[] atomicNodes() { return atomicNodes.clone(); }
    @Override public double[] regionBoundaries() { return regionBoundaries.clone(); }
    @Override public int[] pointsPerRegion() { return pointsPerRegion.clone(); }
    @Override public double[] regionBiases() { return regionBiases.clone(); }
}
