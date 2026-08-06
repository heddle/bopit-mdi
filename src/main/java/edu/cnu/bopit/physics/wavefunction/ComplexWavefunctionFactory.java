package edu.cnu.bopit.physics.wavefunction;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.special.SphericalBessel;

/** Converts a symmetric-basis complex eigenvector to normalized momentum and coordinate waves. */
public final class ComplexWavefunctionFactory {
    private static final int RADIAL_SAMPLES = 501;
    private static final double RADIAL_MAXIMUM_FM = 50.0;

    private ComplexWavefunctionFactory() { }

    public static ComplexWavefunctions create(int orbitalL, MomentumGrid grid,
            Complex[] symmetricEigenvector) {
        int n = grid.size();
        if (symmetricEigenvector == null || symmetricEigenvector.length != n) {
            throw new IllegalArgumentException("eigenvector must match grid");
        }
        Complex cNorm = Complex.ZERO;
        for (Complex value : symmetricEigenvector) cNorm = cNorm.add(value.multiply(value));
        if (cNorm.abs() < 1e-30) throw new IllegalArgumentException("complex c-norm is zero");
        Complex scale = cNorm.sqrt();
        Complex[] phi = new Complex[n];
        Complex after = Complex.ZERO;
        for (int i = 0; i < n; i++) {
            phi[i] = symmetricEigenvector[i].divide(scale)
                    .divide(grid.point(i) * Math.sqrt(grid.weight(i)));
            after = after.add(phi[i].multiply(phi[i])
                    .multiply(grid.weight(i) * grid.point(i) * grid.point(i)));
        }
        var momentum = new ComplexMomentumWavefunction(grid.points(), grid.weights(), phi, cNorm, after);
        double[] radii = new double[RADIAL_SAMPLES];
        Complex[] radial = new Complex[RADIAL_SAMPLES];
        double factor = Math.sqrt(2.0 / Math.PI);
        for (int i = 0; i < RADIAL_SAMPLES; i++) {
            radii[i] = RADIAL_MAXIMUM_FM * i / (RADIAL_SAMPLES - 1.0);
            Complex sum = Complex.ZERO;
            for (int j = 0; j < n; j++) {
                sum = sum.add(phi[j].multiply(grid.weight(j) * grid.point(j) * grid.point(j)
                        * SphericalBessel.value(orbitalL, grid.point(j) * radii[i])));
            }
            radial[i] = sum.multiply(factor);
        }
        return new ComplexWavefunctions(momentum,
                new ComplexCoordinateWavefunction(radii, radial));
    }
}
