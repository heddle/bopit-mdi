package edu.cnu.bopit.calculation.diagnostic;

import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.special.Gegenbauer;

/** Builds the analytic Coulomb momentum-space shape, normalized to unit peak. */
public final class CoulombMomentumProfileFactory {
    private CoulombMomentumProfileFactory() { }

    /**
     * Sample the radial shape proportional to
     * p^l C_(n-l-1)^(l+1)(x)/(p^2+k^2)^(l+2), with
     * x=(p^2-k^2)/(p^2+k^2) and k=Z alpha mu/(n hbar c).
     * The displayed magnitude is normalized to its sampled maximum; it is not
     * presented as a physically normalized wavefunction.
     */
    public static CoulombMomentumProfile create(AtomicSystem atom, QuantumState state,
            PhysicalConstantSet constants, double minimumMomentumFmInverse,
            double maximumMomentumFmInverse, int sampleCount) {
        if (atom == null || state == null || constants == null) {
            throw new IllegalArgumentException("atom, state, and constants are required");
        }
        if (!Double.isFinite(minimumMomentumFmInverse) || minimumMomentumFmInverse <= 0.0
                || !Double.isFinite(maximumMomentumFmInverse)
                || maximumMomentumFmInverse <= minimumMomentumFmInverse
                || sampleCount < 2) {
            throw new IllegalArgumentException("invalid logarithmic sampling range");
        }
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double k = atom.nuclearCharge() * constants.fineStructureConstant() * reducedMass
                / (state.principalN() * constants.hbarCMeVFm());
        double[] momenta = new double[sampleCount];
        double[] magnitude = new double[sampleCount];
        double logMin = Math.log(minimumMomentumFmInverse);
        double logStep = (Math.log(maximumMomentumFmInverse) - logMin) / (sampleCount - 1);
        double maximum = 0.0;
        for (int i = 0; i < sampleCount; i++) {
            double p = Math.exp(logMin + i * logStep);
            momenta[i] = p;
            magnitude[i] = Math.abs(shapeValue(state, k, p));
            maximum = Math.max(maximum, magnitude[i]);
        }
        if (!(maximum > 0.0) || !Double.isFinite(maximum)) {
            throw new ArithmeticException("Coulomb momentum profile has no finite nonzero samples");
        }
        for (int i = 0; i < sampleCount; i++) magnitude[i] /= maximum;
        return new CoulombMomentumProfile(momenta, magnitude);
    }

    /** Unnormalized analytic radial momentum-space Coulomb shape. */
    public static double shapeValue(QuantumState state, double k, double momentum) {
        int n = state.principalN();
        int l = state.orbitalL();
        double p2 = momentum * momentum;
        double k2 = k * k;
        double x = (p2 - k2) / (p2 + k2);
        double polynomial = Gegenbauer.value(n - l - 1, l + 1.0, x);
        return Math.pow(momentum, l) * polynomial / Math.pow(p2 + k2, l + 2.0);
    }
}
