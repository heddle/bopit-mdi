package edu.cnu.bopit.model;

/** Immutable specification of a strong interaction. */
public sealed interface StrongInteractionSpec permits NoStrongInteractionSpec,
        KwonTabakinOpticalPotentialSpec {
}
