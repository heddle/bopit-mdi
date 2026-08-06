package edu.cnu.bopit.model;

/** Immutable specification of the normalized nuclear charge distribution. */
public sealed interface ChargeDistributionSpec permits PointChargeSpec,
        UniformChargeSpec, GaussianChargeSpec, FermiChargeSpec {
}
