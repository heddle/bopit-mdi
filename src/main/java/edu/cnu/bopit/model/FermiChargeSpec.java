package edu.cnu.bopit.model;

/** Three-parameter Fermi distribution; {@code w=0} gives the usual two-parameter form. */
public record FermiChargeSpec(double halfDensityRadiusFm, double diffusenessFm,
        double w) implements ChargeDistributionSpec {
    public FermiChargeSpec {
        if (!Double.isFinite(halfDensityRadiusFm) || halfDensityRadiusFm <= 0.0
                || !Double.isFinite(diffusenessFm) || diffusenessFm <= 0.0
                || !Double.isFinite(w)) {
            throw new IllegalArgumentException("Fermi parameters must be finite and radii positive");
        }
    }
}
