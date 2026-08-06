package edu.cnu.bopit.physics.wavefunction;

import edu.cnu.bopit.grid.MomentumGrid;

/** Converts a symmetric quadrature-basis eigenvector to normalized phi_l(p). */
public final class MomentumWavefunctionFactory {
    private MomentumWavefunctionFactory() { }

    public static MomentumWavefunction create(MomentumGrid grid, double[] symmetricEigenvector) {
        if (grid == null || symmetricEigenvector == null
                || symmetricEigenvector.length != grid.size()) {
            throw new IllegalArgumentException("grid and matching eigenvector are required");
        }
        double normSquared = 0.0;
        for (double value : symmetricEigenvector) normSquared += value * value;
        if (!(normSquared > 0.0) || !Double.isFinite(normSquared)) {
            throw new ArithmeticException("eigenvector has no finite positive norm");
        }
        double normBefore = Math.sqrt(normSquared);
        double[] values = new double[grid.size()];
        double normAfterSquared = 0.0;
        for (int i = 0; i < values.length; i++) {
            values[i] = symmetricEigenvector[i] / normBefore
                    / (grid.point(i) * Math.sqrt(grid.weight(i)));
            normAfterSquared += grid.radialWeight(i) * values[i] * values[i];
        }
        return new MomentumWavefunction(grid.points(), grid.weights(), values,
                normBefore, Math.sqrt(normAfterSquared));
    }
}
