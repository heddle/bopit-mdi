package edu.cnu.bopit.model;

/** Gaussian charge distribution, parameterized by its rms radius in fm. */
public record GaussianChargeSpec(double rmsRadiusFm) implements ChargeDistributionSpec {
    public GaussianChargeSpec {
        if (!Double.isFinite(rmsRadiusFm) || rmsRadiusFm <= 0.0) {
            throw new IllegalArgumentException("rms radius must be finite and positive");
        }
    }
}
