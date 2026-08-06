package edu.cnu.bopit.physics;

/** Exact point-Coulomb Klein-Gordon binding energy for a spin-zero particle. */
public final class KleinGordonCoulombEnergy {
    private KleinGordonCoulombEnergy() { }

    public static double bindingEnergyMeV(double reducedMassMeV, int nuclearCharge,
            double alpha, int principalN, int orbitalL) {
        if (!Double.isFinite(reducedMassMeV) || reducedMassMeV <= 0.0
                || nuclearCharge <= 0 || !Double.isFinite(alpha) || alpha <= 0.0
                || principalN < 1 || orbitalL < 0 || orbitalL >= principalN) {
            throw new IllegalArgumentException("invalid Klein-Gordon Coulomb inputs");
        }
        double za = nuclearCharge * alpha;
        double angular = orbitalL + 0.5;
        if (za >= angular) throw new IllegalArgumentException("supercritical point-Coulomb state");
        double eta = principalN - angular + Math.sqrt(angular * angular - za * za);
        double factor = 1.0 / Math.sqrt(1.0 + za * za / (eta * eta));
        return reducedMassMeV * (factor - 1.0);
    }
}
