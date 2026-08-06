package edu.cnu.bopit.physics.strong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.physics.electromagnetic.GaussianChargeDistribution;

class KwonTabakinOpticalPotentialTest {
    @Test
    void preservesEq24SignAndDimensionsAtZeroMomentum() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var atom = new AtomicSystem(6, 12, OrbitingParticle.KAON_MINUS,
                constants.kaonMassMeV(), constants.nuclearMassMeV(6, 12));
        var scatteringLength = new Complex(0.44, 0.83);
        var model = new KwonTabakinOpticalPotential(atom, constants,
                scatteringLength, new GaussianChargeDistribution(1.64));
        Complex value = model.partialWaveValue(0, 0.0, 0.0);
        double mu = atom.particleMassMeV() * atom.nuclearMassMeV()
                / (atom.particleMassMeV() + atom.nuclearMassMeV());
        Complex expected = scatteringLength.multiply(-Math.pow(constants.hbarCMeVFm(), 2)
                / (2.0 * mu) * (1.0 + atom.particleMassMeV() / constants.protonMassMeV())
                * atom.massNumber() * 2.0 / Math.PI);
        assertEquals(expected.getReal(), value.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), value.getImaginary(), 1e-12);
        assertTrue(value.getReal() < 0.0);
        assertTrue(value.getImaginary() < 0.0);
    }
}
