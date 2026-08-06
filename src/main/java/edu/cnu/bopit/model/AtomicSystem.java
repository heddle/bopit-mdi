package edu.cnu.bopit.model;

/** Immutable masses and charges for a two-body atomic system. */
public record AtomicSystem(int nuclearCharge, int massNumber,
        OrbitingParticle particle, double particleMassMeV, double nuclearMassMeV) {

    public AtomicSystem {
        if (nuclearCharge <= 0) throw new IllegalArgumentException("nuclearCharge must be positive");
        if (massNumber < nuclearCharge) throw new IllegalArgumentException("massNumber must be >= nuclearCharge");
        if (particle == null) throw new IllegalArgumentException("particle is required");
        if (!Double.isFinite(particleMassMeV) || particleMassMeV <= 0.0) {
            throw new IllegalArgumentException("particleMassMeV must be finite and positive");
        }
        if (!Double.isFinite(nuclearMassMeV) || nuclearMassMeV <= 0.0) {
            throw new IllegalArgumentException("nuclearMassMeV must be finite and positive");
        }
        if (particle.chargeNumber() * nuclearCharge >= 0) {
            throw new IllegalArgumentException("Stage 1 requires an attractive Coulomb system");
        }
    }
}
