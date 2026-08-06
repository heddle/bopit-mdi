package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurKleinGordonTest {
    @Test
    void pointCoulombOuterIterationConvergesToExactScalarEnergy() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 60, 10);
        var problem = new BopitProblem(base.atomicSystem(), base.quantumState(),
                new KleinGordonSpec(KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR, 1e-11, 12),
                base.grid(), base.solver(), base.electromagnetic(), base.strongInteraction());
        var result = new KleinGordonCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        assertTrue(result.converged());
        assertTrue(result.outerHistory().size() >= 2);
        assertEquals(result.exactPointCoulombEnergyMeV(), result.bindingEnergyMeV(), 2e-5);
        assertTrue(result.outerHistory().get(result.outerHistory().size() - 1).energyChangeMeV() <= 1e-11);
    }
}
