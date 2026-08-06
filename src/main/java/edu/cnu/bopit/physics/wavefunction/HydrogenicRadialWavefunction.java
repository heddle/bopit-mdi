package edu.cnu.bopit.physics.wavefunction;

import org.apache.commons.math3.util.CombinatoricsUtils;

import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.special.AssociatedLaguerre;

/** Exact normalized coordinate-space Coulomb radial wavefunction R_nl(r). */
public final class HydrogenicRadialWavefunction {
    private HydrogenicRadialWavefunction() { }

    public static double value(QuantumState state, double bohrRadiusFm, double radiusFm) {
        if (state == null || !Double.isFinite(bohrRadiusFm) || bohrRadiusFm <= 0.0
                || !Double.isFinite(radiusFm) || radiusFm < 0.0) {
            throw new IllegalArgumentException("invalid hydrogenic radial-wavefunction input");
        }
        int n = state.principalN();
        int l = state.orbitalL();
        int degree = n - l - 1;
        double rho = 2.0 * radiusFm / (n * bohrRadiusFm);
        double normalization = Math.sqrt(Math.pow(2.0 / (n * bohrRadiusFm), 3)
                * CombinatoricsUtils.factorialDouble(degree)
                / (2.0 * n * CombinatoricsUtils.factorialDouble(n + l)));
        return normalization * Math.exp(-0.5 * rho) * Math.pow(rho, l)
                * AssociatedLaguerre.value(degree, 2.0 * l + 1.0, rho);
    }
}
