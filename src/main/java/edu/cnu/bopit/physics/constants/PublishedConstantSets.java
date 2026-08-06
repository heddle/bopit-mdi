package edu.cnu.bopit.physics.constants;

/** Named constant sets; values are never silently mixed across publications. */
public final class PublishedConstantSets {
    public static final PhysicalConstantSet BOPIT_1990 = new PhysicalConstantSet(
            "bopit-4.0-1990", "BOPIT version 4.0 source, April 1990",
            197.32858, 7.29735e-3, 0.5110034, 493.667, 938.50, 938.50);

    /** 2024 PDG/CODATA values; charged-kaon mass is from the PDG meson table. */
    public static final PhysicalConstantSet PDG_2024 = new PhysicalConstantSet(
            "pdg-2024", "Particle Data Group, Review of Particle Physics 2024",
            197.3269804, 7.2973525693e-3, 0.51099895000, 493.677,
            938.27208816, 939.56542052);

    private PublishedConstantSets() {
    }
}
