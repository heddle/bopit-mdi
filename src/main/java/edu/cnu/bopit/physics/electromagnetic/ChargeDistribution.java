package edu.cnu.bopit.physics.electromagnetic;

/** A spherically symmetric charge density normalized to unit total charge. */
public interface ChargeDistribution {
    /** Normalized density in fm^-3. Point charges do not have a finite density value. */
    double densityFmMinus3(double radiusFm);

    /** Dimensionless Fourier form factor, with {@code F(0)=1}; momentum is in fm^-1. */
    double formFactor(double momentumTransferFmInverse);

    /** Root-mean-square charge radius in fm; zero denotes a point charge. */
    double rmsRadiusFm();

    /** Whether this distribution is a point charge. */
    default boolean isPointCharge() { return false; }
}
