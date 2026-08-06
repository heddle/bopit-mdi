package edu.cnu.bopit.calculation;

/** One retained Klein-Gordon outer energy cycle. */
public record KleinGordonOuterRecord(int cycle, double inputBindingEnergyMeV,
        double epsilonMeV2, double outputBindingEnergyMeV, double energyChangeMeV,
        int innerIterations, double innerResidualMeV2) {
}
