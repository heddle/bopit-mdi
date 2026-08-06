package edu.cnu.bopit.physics.strong;

import org.apache.commons.math3.complex.Complex;

/** Zero strong interaction. */
public final class NoStrongInteraction implements StrongInteractionModel {
    @Override public String name() { return "None"; }
    @Override public Complex partialWaveValue(int orbitalL, double p, double pPrime) {
        return Complex.ZERO;
    }
}
