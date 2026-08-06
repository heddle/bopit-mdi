package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurTableIIIRegressionTest {
    @Test void reproducesKwonTabakinMomentumSpaceShiftAndWidth() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32TableIII(constants, 80, 20);
        var result = new ComplexKleinGordonCalculator().calculate(
                problem, constants, CalculationMonitor.NONE);
        assertTrue(result.converged());
        assertEquals(0.000503, result.strongInteractionShiftMeV(), 5e-6);
        assertEquals(0.002317, result.widthMeV(), 3e-5);
    }
}
