package edu.cnu.bopit.model;

/** Dirac equation selection; total angular momentum is represented by kappa. */
public record DiracSpec(int kappa) implements WaveEquationSpec {
    public DiracSpec {
        if (kappa == 0) throw new IllegalArgumentException("Dirac kappa cannot be zero");
    }
}
