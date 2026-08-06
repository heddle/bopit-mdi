package edu.cnu.bopit.model;

/** Immutable selection of the bound-state wave equation. */
public sealed interface WaveEquationSpec permits SchrodingerSpec,
        RelativisticSchrodingerSpec, KleinGordonSpec, DiracSpec {
}
