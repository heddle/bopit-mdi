package edu.cnu.bopit.model;

/** Orbiting particle supported by the first vertical slice. */
public enum OrbitingParticle {
    KAON_MINUS("K-", -1);

    private final String symbol;
    private final int chargeNumber;

    OrbitingParticle(String symbol, int chargeNumber) {
        this.symbol = symbol;
        this.chargeNumber = chargeNumber;
    }

    public String symbol() {
        return symbol;
    }

    public int chargeNumber() {
        return chargeNumber;
    }
}
