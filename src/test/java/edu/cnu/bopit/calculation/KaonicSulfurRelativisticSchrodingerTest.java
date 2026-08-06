package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurRelativisticSchrodingerTest {
    @Test
    void relativisticKineticCorrectionMakesStateMoreBound() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var ordinary = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var relativistic = new BopitProblem(ordinary.atomicSystem(), ordinary.quantumState(),
                new RelativisticSchrodingerSpec(), ordinary.grid(), ordinary.solver(),
                ordinary.electromagnetic(), ordinary.strongInteraction());
        var calculator = new PointCoulombCalculator();
        var ordinaryResult = calculator.calculate(ordinary, constants, CalculationMonitor.NONE);
        var relativisticResult = calculator.calculate(relativistic, constants, CalculationMonitor.NONE);
        assertTrue(relativisticResult.solverResult().converged());
        assertTrue(relativisticResult.calculatedEnergyMeV() < ordinaryResult.calculatedEnergyMeV());
    }
}
