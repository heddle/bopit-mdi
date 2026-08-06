package edu.cnu.bopit.model;

/** Nonrelativistic hydrogenic quantum numbers. */
public record QuantumState(int principalN, int orbitalL) {
    public QuantumState {
        if (principalN < 1) throw new IllegalArgumentException("principalN must be >= 1");
        if (orbitalL < 0 || orbitalL >= principalN) {
            throw new IllegalArgumentException("orbitalL must satisfy 0 <= l < n");
        }
    }
}
