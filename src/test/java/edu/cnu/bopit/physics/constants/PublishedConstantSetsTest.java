package edu.cnu.bopit.physics.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PublishedConstantSetsTest {
    @Test void retainsHistoricalSetAndProvidesPdg2024Set() {
        assertEquals(493.667, PublishedConstantSets.BOPIT_1990.kaonMassMeV());
        var pdg = PublishedConstantSets.PDG_2024;
        assertEquals(197.3269804, pdg.hbarCMeVFm());
        assertEquals(7.2973525693e-3, pdg.fineStructureConstant());
        assertEquals(0.51099895000, pdg.electronMassMeV());
        assertEquals(493.677, pdg.kaonMassMeV());
        assertEquals(938.27208816, pdg.protonMassMeV());
        assertEquals(939.56542052, pdg.neutronMassMeV());
    }
}
