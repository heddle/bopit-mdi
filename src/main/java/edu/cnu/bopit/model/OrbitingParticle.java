package edu.cnu.bopit.model;

/** Supported negatively charged orbiting particles with PDG-2024 recommended masses. */
public enum OrbitingParticle {
    ELECTRON("e-", "electron", -1, 1, 0.51099895000),
    KAON_MINUS("K-", "kaon", -1, 0, 493.677),
    MUON_MINUS("mu-", "muon", -1, 1, 105.6583755),
    PION_MINUS("pi-", "pion", -1, 0, 139.57039);

    private final String symbol;
    private final String name;
    private final int chargeNumber;
    private final int twiceSpin;
    private final double recommendedMassMeV;

    OrbitingParticle(String symbol, String name, int chargeNumber, int twiceSpin,
            double recommendedMassMeV) {
        this.symbol = symbol;
        this.name = name;
        this.chargeNumber = chargeNumber;
        this.twiceSpin = twiceSpin;
        this.recommendedMassMeV = recommendedMassMeV;
    }

    public String symbol() {
        return symbol;
    }

    public int chargeNumber() {
        return chargeNumber;
    }

    public int twiceSpin() { return twiceSpin; }

    /** Recommended rest mass in MeV from the 2024 PDG summary tables. */
    public double recommendedMassMeV() { return recommendedMassMeV; }

    @Override public String toString() { return symbol + " (" + name + ")"; }
}
