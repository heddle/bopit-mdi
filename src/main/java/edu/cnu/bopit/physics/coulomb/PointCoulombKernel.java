package edu.cnu.bopit.physics.coulomb;

/** Attractive point-Coulomb kernel in MeV fm^3 for momenta in fm^-1. */
public final class PointCoulombKernel implements CoulombKernel {
    private final double coefficientMeVFm;

    public PointCoulombKernel(int nuclearCharge, double alpha, double hbarCMeVFm) {
        if (nuclearCharge <= 0 || alpha <= 0.0 || hbarCMeVFm <= 0.0) {
            throw new IllegalArgumentException("Coulomb constants must be positive");
        }
        coefficientMeVFm = nuclearCharge * alpha * hbarCMeVFm / Math.PI;
    }

    @Override
    public double offDiagonal(int orbitalL, double momentum, double otherMomentum) {
        if (orbitalL < 0 || !Double.isFinite(momentum) || momentum <= 0.0
                || !Double.isFinite(otherMomentum) || otherMomentum <= 0.0) {
            throw new IllegalArgumentException("invalid kernel input");
        }
        if (momentum == otherMomentum) {
            throw new IllegalArgumentException("the singular point-Coulomb diagonal must use Landé subtraction");
        }
        double ratio = momentum / otherMomentum;
        double z = 0.5 * (ratio + 1.0 / ratio);
        if (z <= 1.0) {
            throw new ArithmeticException("rounding collapsed Coulomb argument onto its singular point");
        }
        return -coefficientMeVFm * LegendreQ.value(orbitalL, z) / (momentum * otherMomentum);
    }
}
