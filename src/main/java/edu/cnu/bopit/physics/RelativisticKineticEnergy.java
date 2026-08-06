package edu.cnu.bopit.physics;

/** Stable two-body relativistic kinetic energy in the center-of-momentum frame. */
public final class RelativisticKineticEnergy {
    private RelativisticKineticEnergy() { }

    /** sqrt((hbar*c*p)^2+m^2)-m for both bodies, returned in MeV. */
    public static double twoBodyMeV(double pFmInverse, double particleMassMeV,
            double nuclearMassMeV, double hbarCMeVFm) {
        requirePositive(particleMassMeV, "particle mass");
        requirePositive(nuclearMassMeV, "nuclear mass");
        requirePositive(hbarCMeVFm, "hbar c");
        if (!Double.isFinite(pFmInverse) || pFmInverse < 0.0) {
            throw new IllegalArgumentException("momentum must be finite and nonnegative");
        }
        double pc = hbarCMeVFm * pFmInverse;
        return oneBody(pc, particleMassMeV) + oneBody(pc, nuclearMassMeV);
    }

    private static double oneBody(double pc, double mass) {
        // Rationalized form avoids subtracting nearly equal numbers at atomic momenta.
        return pc * pc / (Math.hypot(pc, mass) + mass);
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }
}
