package edu.cnu.bopit.model;

/** Electromagnetic corrections applied to the point-Coulomb Hamiltonian. */
public record ElectromagneticSpec(ChargeDistributionSpec nuclearCharge,
        boolean uehlingVacuumPolarization) {
    public static final ElectromagneticSpec POINT_COULOMB =
            new ElectromagneticSpec(new PointChargeSpec(), false);

    public ElectromagneticSpec {
        if (nuclearCharge == null) {
            throw new IllegalArgumentException("nuclear charge specification is required");
        }
    }
}
