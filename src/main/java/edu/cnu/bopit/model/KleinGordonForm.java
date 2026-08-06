package edu.cnu.bopit.model;

/** Explicitly named Klein-Gordon interaction forms corresponding to historical IKG choices. */
public enum KleinGordonForm {
    /** (E-Vc)^2 = p^2 + mu^2 + 2 E Vn. */
    ENERGY_WEIGHTED_NUCLEAR,
    /** (E-Vc-Vn)^2 = p^2 + mu^2. */
    FULL_VECTOR,
    /** (E-Vc)^2 = p^2 + mu^2 + 2 mu Vn. */
    MASS_WEIGHTED_NUCLEAR,
    /** (E-Vc)^2 = p^2 + (mu+Vn)^2. */
    SCALAR_NUCLEAR
}
