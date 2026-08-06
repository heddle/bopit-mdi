package edu.cnu.bopit.study;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class NamedStudyTemplatesTest {
    @Test
    void exposesEveryDocumentedTemplate() {
        var base = PublishedProblems.kaonicSulfur32Legacy3d(PublishedConstantSets.BOPIT_1990, 40, 10);
        List<ParameterStudy> studies = List.of(NamedStudyTemplates.gridConvergence(base),
                NamedStudyTemplates.cutoffStability(base),
                NamedStudyTemplates.adaptiveVersusLegacy(base), NamedStudyTemplates.regionBias(base),
                NamedStudyTemplates.strongPotentialSensitivity(base),
                NamedStudyTemplates.finiteSizeSensitivity(base),
                NamedStudyTemplates.waveEquationComparison(base));
        assertEquals(7, studies.size());
        studies.forEach(study -> assertEquals(1, study.axes().size()));
    }
}
