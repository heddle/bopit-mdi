package edu.cnu.bopit.physics.wavefunction;

import edu.cnu.bopit.physics.special.SphericalBessel;

/** Unitary radial Fourier-Bessel transform for p in fm^-1 and r in fm. */
public final class FourierBesselTransform {
    private static final double UNITARY_FACTOR = Math.sqrt(2.0 / Math.PI);

    private FourierBesselTransform() { }

    /**
     * R_l(r) = sqrt(2/pi) integral p^2 j_l(pr) phi_l(p) dp, evaluated with
     * the momentum wavefunction's quadrature weights.
     */
    public static CoordinateWavefunction transform(int orbitalL,
            MomentumWavefunction momentumWavefunction, double[] radiiFm) {
        if (orbitalL < 0 || momentumWavefunction == null || radiiFm == null
                || radiiFm.length < 2) {
            throw new IllegalArgumentException("orbital order, wavefunction, and radii are required");
        }
        double[] p = momentumWavefunction.momentaFmInverse();
        double[] w = momentumWavefunction.weightsFmInverse();
        double[] phi = momentumWavefunction.radialValuesFmThreeHalves();
        double[] radial = new double[radiiFm.length];
        double previousRadius = -1.0;
        for (int i = 0; i < radiiFm.length; i++) {
            double radius = radiiFm[i];
            if (!Double.isFinite(radius) || radius < 0.0 || radius <= previousRadius) {
                throw new IllegalArgumentException("radii must be finite, nonnegative, and ordered");
            }
            double sum = 0.0;
            for (int j = 0; j < p.length; j++) {
                sum += w[j] * p[j] * p[j]
                        * SphericalBessel.value(orbitalL, p[j] * radius) * phi[j];
            }
            radial[i] = UNITARY_FACTOR * sum;
            previousRadius = radius;
        }
        return new CoordinateWavefunction(radiiFm, radial);
    }
}
