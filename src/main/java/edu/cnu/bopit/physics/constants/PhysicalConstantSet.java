package edu.cnu.bopit.physics.constants;

/** Immutable physical constants with explicit provenance. */
public record PhysicalConstantSet(String id, String source, double hbarCMeVFm,
        double fineStructureConstant, double electronMassMeV, double kaonMassMeV,
        double protonMassMeV, double neutronMassMeV) {
    public PhysicalConstantSet {
        if (id == null || id.isBlank() || source == null || source.isBlank()) {
            throw new IllegalArgumentException("id and source are required");
        }
        requirePositive(hbarCMeVFm, "hbarCMeVFm");
        requirePositive(fineStructureConstant, "fineStructureConstant");
        requirePositive(electronMassMeV, "electronMassMeV");
        requirePositive(kaonMassMeV, "kaonMassMeV");
        requirePositive(protonMassMeV, "protonMassMeV");
        requirePositive(neutronMassMeV, "neutronMassMeV");
    }

    public double nuclearMassMeV(int protons, int massNumber) {
        if (protons <= 0 || massNumber < protons) throw new IllegalArgumentException("invalid nucleus");
        return protons * protonMassMeV + (massNumber - protons) * neutronMassMeV;
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }
}
