package edu.cnu.bopit.solver;

import org.apache.commons.math3.complex.Complex;

/** One complex inverse-iteration diagnostic sample. */
public record ComplexIterationRecord(int iteration, Complex eigenvalueMeV,
        double energyChangeMeV, double residualNorm) {
}
