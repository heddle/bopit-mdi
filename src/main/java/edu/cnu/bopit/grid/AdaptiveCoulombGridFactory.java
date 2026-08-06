package edu.cnu.bopit.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.special.Gegenbauer;

/** Modern, diagnostic implementation of BOPIT's {@code AUTOGAUS}. */
public final class AdaptiveCoulombGridFactory {
    private static final double PI_OVER_FOUR = Math.PI / 4.0;
    private final GaussLegendreRules rules = new GaussLegendreRules();

    public MomentumGrid create(AdaptiveGridSpec spec, AtomicSystem atom,
            QuantumState state, PhysicalConstantSet constants) {
        int lobes = state.principalN() - state.orbitalL();
        int perLobe = (spec.totalPoints() - spec.nuclearPoints()) / lobes;
        if ((perLobe & 1) != 0) perLobe--;
        if (perLobe < 2) throw new IllegalArgumentException("adaptive grid needs at least two points per atomic lobe");
        int actualTotal = spec.nuclearPoints() + lobes * perLobe;
        List<String> warnings = new ArrayList<>();
        if (actualTotal != spec.totalPoints()) {
            warnings.add("Total point count adjusted from " + spec.totalPoints() + " to " + actualTotal
                    + " to give every atomic lobe an even Gauss-Legendre order.");
        }

        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double za = atom.nuclearCharge() * constants.fineStructureConstant()
                * reducedMass / constants.hbarCMeVFm();
        double atomicScale = za / (state.principalN() * (double) state.principalN());
        double nuclearRadius = 1.12 * Math.pow(atom.massNumber(), 0.3333);
        double nuclearStart = 0.5 * (1.0 + 0.075 * (atom.nuclearCharge() - 1.0)) / nuclearRadius;
        double concentrationMomentum = 2.0 - (state.principalN() - 1.0) * 0.242;
        if (concentrationMomentum <= nuclearStart) concentrationMomentum = 50.0 * nuclearStart;
        double maximum = spec.maximumMomentum();
        if (maximum <= nuclearStart) {
            maximum = 1_000.0 * nuclearStart;
            warnings.add("Maximum momentum increased to remain above the nuclear-region boundary.");
        }

        double minimum = 1.0e-6 * atomicScale;
        double computerFloor = 0.5 * maximum
                / Math.pow(1.0e36, 1.0 / (state.orbitalL() + 1.0));
        minimum = Math.max(minimum, computerFloor);
        if (lobes == 1) minimum /= Math.pow(10.0, state.principalN());

        double[] roots = Gegenbauer.zeros(lobes - 1, state.orbitalL() + 1.0);
        double[] nodes = new double[roots.length];
        for (int i = 0; i < roots.length; i++) {
            nodes[i] = za * Math.sqrt((1.0 + roots[i]) / (1.0 - roots[i]))
                    / state.principalN();
        }
        if (nodes.length > 0 && minimum >= nodes[0]) {
            minimum = 0.1 * nodes[0];
            warnings.add("Minimum momentum reduced to remain below the last atomic node.");
        }
        if (nodes.length >= 1
                && nodes[nodes.length - 1]
                        - (nodes.length >= 2 ? nodes[nodes.length - 2] : minimum)
                        > nuclearStart - nodes[nodes.length - 1]) {
            nuclearStart = 3.0 * nodes[nodes.length - 1];
            warnings.add("Nuclear-region boundary moved outward to separate it from the last atomic node.");
        }
        if (nuclearStart >= maximum) {
            maximum = 100.0 * nuclearStart;
            warnings.add("Maximum momentum increased after nuclear-boundary repair.");
        }

        double[] boundaries = new double[lobes + 2];
        boundaries[0] = minimum;
        System.arraycopy(nodes, 0, boundaries, 1, nodes.length);
        boundaries[lobes] = nuclearStart;
        boundaries[lobes + 1] = maximum;
        validateBoundaries(boundaries);

        double[] points = new double[actualTotal];
        double[] weights = new double[actualTotal];
        List<GridRegion> regions = new ArrayList<>();
        int[] counts = new int[lobes + 1];
        double[] biases = new double[lobes + 1];
        Arrays.fill(counts, 0, lobes, perLobe);
        counts[lobes] = spec.nuclearPoints();

        int offset = 0;
        for (int region = 0; region < lobes; region++) {
            double lower = boundaries[region];
            double upper = boundaries[region + 1];
            int historicalK = lobes - region;
            double bias = spec.regionBias();
            if (historicalK == 1) {
                double coefficient = lobes > 1 ? (lobes - 1.0) * 5.0 : state.principalN();
                bias = Math.min(0.2, coefficient * atomicScale / nuclearStart);
            }
            biases[region] = bias;
            // AUTOGAUS anchors the lowest lobe at zero rather than CPMIN.
            double mappingBase = region == 0 ? 0.0 : lower;
            mapFiniteRegion(rules.create(perLobe), offset, mappingBase,
                    upper - lower, bias, points, weights);
            regions.add(new GridRegion(GridRegion.Type.ATOMIC, offset, offset + perLobe,
                    mappingBase, upper));
            offset += perLobe;
        }

        double nuclearBias = (concentrationMomentum - nuclearStart) / (maximum - nuclearStart);
        if (!(nuclearBias > 0.0 && nuclearBias < 1.0)) {
            nuclearBias = spec.regionBias();
            warnings.add("Nuclear-region concentration was outside its boundaries; regionBias was used.");
        }
        biases[lobes] = nuclearBias;
        mapFiniteRegion(rules.create(spec.nuclearPoints()), offset, nuclearStart,
                maximum - nuclearStart, nuclearBias, points, weights);
        regions.add(new GridRegion(GridRegion.Type.NUCLEAR, offset, actualTotal,
                nuclearStart, maximum));

        AdaptiveGridDiagnostics diagnostics = new AdaptiveGridDiagnostics(za, atomicScale,
                minimum, nuclearStart, maximum, nodes, boundaries, counts,
                biases, spec.totalPoints(), actualTotal, warnings);
        return new MomentumGrid(points, weights, regions, diagnostics);
    }

    private static void mapFiniteRegion(GaussLegendreRule rule, int offset,
            double base, double width, double bias, double[] points, double[] weights) {
        double ratio = bias / (1.0 - bias);
        double scale = width * ratio;
        for (int i = 0; i < rule.size(); i++) {
            double theta = PI_OVER_FOUR * (1.0 + rule.node(i));
            double sine = Math.sin(theta);
            double cosine = Math.cos(theta);
            double denominator = cosine + ratio * sine;
            points[offset + i] = base + scale * sine / denominator;
            weights[offset + i] = rule.weight(i) * scale * PI_OVER_FOUR
                    / (denominator * denominator);
        }
    }

    private static void validateBoundaries(double[] boundaries) {
        for (int i = 0; i < boundaries.length; i++) {
            if (!Double.isFinite(boundaries[i]) || boundaries[i] <= 0.0
                    || (i > 0 && boundaries[i] <= boundaries[i - 1])) {
                throw new IllegalArgumentException("adaptive boundaries must be finite, positive, and ordered");
            }
        }
    }
}
