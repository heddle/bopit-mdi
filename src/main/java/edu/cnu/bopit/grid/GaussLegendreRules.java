package edu.cnu.bopit.grid;

import org.apache.commons.math3.analysis.integration.gauss.GaussIntegrator;
import org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorFactory;

/** Apache Commons Math adapter for Gauss-Legendre rules. */
public final class GaussLegendreRules {
    private final GaussIntegratorFactory factory = new GaussIntegratorFactory();

    public GaussLegendreRule create(int order) {
        if (order < 1) throw new IllegalArgumentException("order must be positive");
        GaussIntegrator integrator = factory.legendre(order);
        double[] nodes = new double[order];
        double[] weights = new double[order];
        for (int i = 0; i < order; i++) {
            nodes[i] = integrator.getPoint(i);
            weights[i] = integrator.getWeight(i);
        }
        return new GaussLegendreRule(nodes, weights);
    }
}
