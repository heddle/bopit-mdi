package edu.cnu.bopit.calculation;

import org.apache.commons.math3.complex.Complex;

/** One retained complex Klein-Gordon outer cycle. */
public record ComplexKleinGordonOuterRecord(int cycle, Complex inputBindingEnergyMeV,
        Complex epsilonMeV2, Complex outputBindingEnergyMeV, double energyChangeMeV,
        int innerIterations, double innerResidualMeV2) {
}
