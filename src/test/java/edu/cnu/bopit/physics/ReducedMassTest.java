package edu.cnu.bopit.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class ReducedMassTest {
    @Test
    void reproducesBopit1990DerivedValues() {
        var constants = PublishedConstantSets.BOPIT_1990;
        double nucleus = constants.nuclearMassMeV(16, 32);
        double reduced = ReducedMass.of(constants.kaonMassMeV(), nucleus);
        assertEquals(30_032.0, nucleus);
        assertEquals(485.683321645, reduced, 5e-10);
        assertEquals(-0.367833266254,
                ReducedMass.exactCoulombEnergyMeV(reduced, 16,
                        constants.fineStructureConstant(), 3), 5e-13);
    }
}
