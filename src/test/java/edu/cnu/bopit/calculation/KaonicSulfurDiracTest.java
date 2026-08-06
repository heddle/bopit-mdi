package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurDiracTest {
    @Test
    void pointCoulombCoupledComponentsConvergeToExactEnergy() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 60, 10);
        var problem = new BopitProblem(base.atomicSystem(), base.quantumState(),
                new DiracSpec(-3), base.grid(), base.solver(), base.electromagnetic(),
                base.strongInteraction());
        var result = new DiracCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        assertTrue(result.solverResult().converged());
        assertEquals(result.exactPointCoulombEnergyMeV(), result.bindingEnergyMeV(), 2e-5);
        assertEquals(1.0, result.largeComponentNorm() + result.smallComponentNorm(), 2e-12);
        assertTrue(result.smallComponentNorm() > 0.0);
        assertTrue(result.smallComponentNorm() < result.largeComponentNorm());
    }
}
