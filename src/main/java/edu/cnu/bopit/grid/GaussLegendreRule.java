package edu.cnu.bopit.grid;

/** Immutable Gauss-Legendre nodes and weights on [-1, 1]. */
public record GaussLegendreRule(double[] nodes, double[] weights) {
    public GaussLegendreRule {
        if (nodes == null || weights == null || nodes.length == 0 || nodes.length != weights.length) {
            throw new IllegalArgumentException("nodes and weights must have equal positive length");
        }
        nodes = nodes.clone();
        weights = weights.clone();
    }

    @Override public double[] nodes() { return nodes.clone(); }
    @Override public double[] weights() { return weights.clone(); }
    public int size() { return nodes.length; }
    public double node(int index) { return nodes[index]; }
    public double weight(int index) { return weights[index]; }
}
