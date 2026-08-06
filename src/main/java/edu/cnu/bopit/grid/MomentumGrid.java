package edu.cnu.bopit.grid;

import java.util.List;

/** Immutable momentum quadrature in fm^-1 with weights in fm^-1. */
public record MomentumGrid(double[] points, double[] weights, List<GridRegion> regions) {
    public MomentumGrid {
        if (points == null || weights == null || points.length == 0 || points.length != weights.length) {
            throw new IllegalArgumentException("points and weights must have equal positive length");
        }
        points = points.clone();
        weights = weights.clone();
        regions = List.copyOf(regions);
        validate(points, weights);
    }

    @Override public double[] points() { return points.clone(); }
    @Override public double[] weights() { return weights.clone(); }
    public int size() { return points.length; }
    public double point(int index) { return points[index]; }
    public double weight(int index) { return weights[index]; }
    public double radialWeight(int index) { return points[index] * points[index] * weights[index]; }

    private static void validate(double[] points, double[] weights) {
        double previous = 0.0;
        for (int i = 0; i < points.length; i++) {
            if (!Double.isFinite(points[i]) || points[i] <= previous) {
                throw new IllegalArgumentException("points must be finite, positive, and strictly ordered");
            }
            if (!Double.isFinite(weights[i]) || weights[i] <= 0.0) {
                throw new IllegalArgumentException("weights must be finite and positive");
            }
            previous = points[i];
        }
    }
}
