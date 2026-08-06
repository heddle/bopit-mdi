package edu.cnu.bopit.grid;

import java.util.List;

import edu.cnu.bopit.model.LegacyGridSpec;

/** Implements the two tangent mappings in BOPIT's {@code GAUSPT}. */
public final class LegacyMappedGridFactory {
    private static final double PI_OVER_FOUR = Math.PI / 4.0;
    private final GaussLegendreRules rules;

    public LegacyMappedGridFactory() {
        this(new GaussLegendreRules());
    }

    LegacyMappedGridFactory(GaussLegendreRules rules) {
        this.rules = rules;
    }

    public MomentumGrid create(LegacyGridSpec spec) {
        double[] points = new double[spec.totalPoints()];
        double[] weights = new double[spec.totalPoints()];
        mapRegion(rules.create(spec.atomicPoints()), 0, 0.0,
                spec.atomicScale(), spec.atomicScale() / spec.regionBoundary(), points, weights);
        mapRegion(rules.create(spec.nuclearPoints()), spec.atomicPoints(), spec.regionBoundary(),
                spec.nuclearScale(), spec.nuclearScale() / spec.maximumMomentumScale(), points, weights);
        return new MomentumGrid(points, weights, List.of(
                new GridRegion(GridRegion.Type.ATOMIC, 0, spec.atomicPoints(), 0.0, spec.regionBoundary()),
                new GridRegion(GridRegion.Type.NUCLEAR, spec.atomicPoints(), spec.totalPoints(),
                        spec.regionBoundary(), spec.regionBoundary() + spec.maximumMomentumScale())));
    }

    private static void mapRegion(GaussLegendreRule rule, int offset, double base,
            double scale, double ratio, double[] points, double[] weights) {
        for (int i = 0; i < rule.size(); i++) {
            double theta = PI_OVER_FOUR * (1.0 + rule.node(i));
            double sine = Math.sin(theta);
            double cosine = Math.cos(theta);
            double denominator = cosine + ratio * sine;
            points[offset + i] = base + scale * sine / denominator;
            double jacobian = scale * PI_OVER_FOUR / (denominator * denominator);
            weights[offset + i] = rule.weight(i) * jacobian;
        }
    }
}
