package edu.cnu.bopit.calculation.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class LandeIntegrandProfileFactoryTest {
    @Test
    void profileOmitsSingularDiagonalAndHasFiniteTerms() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var result = new PointCoulombCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        int selected = 12;
        var profile = LandeIntegrandProfileFactory.create(problem, result, constants, selected);
        assertEquals(result.grid().size() - 1, profile.otherMomentaFmInverse().length);
        for (double momentum : profile.otherMomentaFmInverse()) {
            assertTrue(momentum != result.grid().point(selected));
        }
        for (double value : profile.regularizedIntegrand()) assertTrue(Double.isFinite(value));
        for (int i = 0; i < profile.regularizedIntegrand().length; i++) {
            assertEquals(profile.ordinaryIntegrand()[i] - profile.subtractionIntegrand()[i],
                    profile.regularizedIntegrand()[i], 0.0);
        }
        assertEquals(result.landeDiagnostics().effectiveDiagonalKernels()[selected],
                profile.effectiveDiagonalKernel(), 0.0);
    }
}
