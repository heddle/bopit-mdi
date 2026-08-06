package edu.cnu.bopit.model;

import org.apache.commons.math3.complex.Complex;

/**
 * Parameters for Kwon and Tabakin (1978), Eq. (24). The fitted scattering
 * length is per nucleon; matrix assembly supplies the density normalization A.
 */
public record KwonTabakinOpticalPotentialSpec(Complex fittedScatteringLengthFm,
        ChargeDistributionSpec nuclearFormFactor) implements StrongInteractionSpec {
    public KwonTabakinOpticalPotentialSpec {
        if (fittedScatteringLengthFm == null || !Double.isFinite(fittedScatteringLengthFm.getReal())
                || !Double.isFinite(fittedScatteringLengthFm.getImaginary())
                || nuclearFormFactor == null) {
            throw new IllegalArgumentException("finite scattering length and nuclear form factor are required");
        }
    }
}
