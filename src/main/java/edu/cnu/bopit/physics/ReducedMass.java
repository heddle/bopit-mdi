package edu.cnu.bopit.physics;

/** Two-body reduced-mass and exact Coulomb-energy formulas. */
public final class ReducedMass {
    private ReducedMass() {
    }

    public static double of(double particleMassMeV, double nuclearMassMeV) {
        if (!Double.isFinite(particleMassMeV) || particleMassMeV <= 0.0
                || !Double.isFinite(nuclearMassMeV) || nuclearMassMeV <= 0.0) {
            throw new IllegalArgumentException("masses must be finite and positive");
        }
        return particleMassMeV * nuclearMassMeV / (particleMassMeV + nuclearMassMeV);
    }

    public static double exactCoulombEnergyMeV(double reducedMassMeV,
            int nuclearCharge, double alpha, int principalN) {
        if (reducedMassMeV <= 0.0 || nuclearCharge <= 0 || alpha <= 0.0 || principalN <= 0) {
            throw new IllegalArgumentException("invalid Coulomb-energy input");
        }
        double coupling = nuclearCharge * alpha / principalN;
        return -0.5 * reducedMassMeV * coupling * coupling;
    }
}
