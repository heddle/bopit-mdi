package edu.cnu.bopit.model;

/** Uniform spherical charge distribution, parameterized by its rms radius in fm. */
public record UniformChargeSpec(double rmsRadiusFm) implements ChargeDistributionSpec {
    public UniformChargeSpec {
        requirePositive(rmsRadiusFm);
    }

    private static void requirePositive(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("rms radius must be finite and positive");
        }
    }
}
