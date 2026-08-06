package edu.cnu.bopit.physics.coulomb;

/** Off-diagonal partial-wave Coulomb kernel. */
@FunctionalInterface
public interface CoulombKernel {
    double offDiagonal(int orbitalL, double momentum, double otherMomentum);
}
