package edu.cnu.bopit.physics.strong;

import org.apache.commons.math3.complex.Complex;

/** Headless partial-wave strong interaction in MeV fm^3. */
public interface StrongInteractionModel {
    String name();

    Complex partialWaveValue(int orbitalL, double pFmInverse, double pPrimeFmInverse);
}
