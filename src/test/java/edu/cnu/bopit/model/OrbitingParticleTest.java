package edu.cnu.bopit.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OrbitingParticleTest {
    @Test void exposesPdg2024MassesAndSpinMetadata() {
        assertEquals(0.51099895000, OrbitingParticle.ELECTRON.recommendedMassMeV());
        assertEquals(105.6583755, OrbitingParticle.MUON_MINUS.recommendedMassMeV());
        assertEquals(139.57039, OrbitingParticle.PION_MINUS.recommendedMassMeV());
        assertEquals(493.677, OrbitingParticle.KAON_MINUS.recommendedMassMeV());
        assertEquals(1, OrbitingParticle.ELECTRON.twiceSpin());
        assertEquals(0, OrbitingParticle.PION_MINUS.twiceSpin());
        assertEquals("K- (kaon)", OrbitingParticle.KAON_MINUS.toString());
    }
}
