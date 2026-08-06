package edu.cnu.bopit.calculation.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class CoulombMomentumProfileFactoryTest {
    @Test
    void normalizedProfileIsPositiveAndPeaksAtOne() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var atom = new AtomicSystem(16, 32, OrbitingParticle.KAON_MINUS,
                constants.kaonMassMeV(), constants.nuclearMassMeV(16, 32));
        var profile = CoulombMomentumProfileFactory.create(atom, new QuantumState(3, 2),
                constants, 1e-5, 1e3, 401);
        double maximum = 0.0;
        for (double value : profile.normalizedMagnitude()) maximum = Math.max(maximum, value);
        assertEquals(1.0, maximum, 1e-15);
        assertTrue(profile.momentaFmInverse()[0] > 0.0);
    }

    @Test
    void twoSShapeHasNodeAtCoulombMomentumScale() {
        assertEquals(0.0,
                CoulombMomentumProfileFactory.shapeValue(new QuantumState(2, 0), 0.2, 0.2),
                1e-14);
    }
}
