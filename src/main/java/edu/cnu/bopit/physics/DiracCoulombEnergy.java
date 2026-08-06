package edu.cnu.bopit.physics;

/** Exact point-Coulomb Dirac binding energy for reduced mass mu. */
public final class DiracCoulombEnergy {
    private DiracCoulombEnergy() { }

    public static double bindingEnergyMeV(double reducedMassMeV, int nuclearCharge,
            double alpha, int principalN, int kappa) {
        if (!Double.isFinite(reducedMassMeV) || reducedMassMeV <= 0.0
                || nuclearCharge <= 0 || !Double.isFinite(alpha) || alpha <= 0.0
                || principalN < 1 || kappa == 0 || principalN < Math.abs(kappa)) {
            throw new IllegalArgumentException("invalid Dirac Coulomb inputs");
        }
        double za = nuclearCharge * alpha;
        double absKappa = Math.abs(kappa);
        if (za >= absKappa) throw new IllegalArgumentException("supercritical point-Coulomb state");
        double eta = principalN - absKappa + Math.sqrt(absKappa * absKappa - za * za);
        double factor = 1.0 / Math.sqrt(1.0 + za * za / (eta * eta));
        return reducedMassMeV * (factor - 1.0);
    }
}
