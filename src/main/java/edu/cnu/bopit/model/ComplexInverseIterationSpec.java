package edu.cnu.bopit.model;

import org.apache.commons.math3.complex.Complex;

/** Convergence controls for complex shifted inverse iteration. */
public record ComplexInverseIterationSpec(Complex shiftMeV, double energyToleranceMeV,
        double residualTolerance, int minimumIterations, int maximumIterations) {
    public ComplexInverseIterationSpec {
        if (shiftMeV == null || !Double.isFinite(shiftMeV.getReal())
                || !Double.isFinite(shiftMeV.getImaginary())
                || !Double.isFinite(energyToleranceMeV) || energyToleranceMeV <= 0.0
                || !Double.isFinite(residualTolerance) || residualTolerance <= 0.0
                || minimumIterations < 1 || maximumIterations < minimumIterations) {
            throw new IllegalArgumentException("invalid complex inverse-iteration specification");
        }
    }
}
