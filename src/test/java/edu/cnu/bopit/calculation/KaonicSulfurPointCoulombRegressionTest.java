package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurPointCoulombRegressionTest {
    @Test
    void solvesBopit1990LegacyGridCase() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        PointCoulombResult result = new PointCoulombCalculator().calculate(
                problem, constants, CalculationMonitor.NONE);
        assertTrue(result.solverResult().converged());
        assertEquals(-0.367833266254, result.referenceEnergyMeV(), 5e-13);
        assertEquals(-0.367806279690, result.calculatedEnergyMeV(), 2e-12);
        assertEquals(7.33663e-5, result.relativeError(), 2e-10);
        assertTrue(result.solverResult().history().get(
                result.solverResult().history().size() - 1).residualNorm() <= 1e-10);
        var matrixCopy = result.coulombOperatorMeV();
        double original = matrixCopy.getEntry(0, 0);
        matrixCopy.setEntry(0, 0, original + 1.0);
        assertNotEquals(matrixCopy.getEntry(0, 0), result.coulombOperatorMeV().getEntry(0, 0));
    }

    @Test
    void improvesUnderLegacyGridRefinement() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var calculator = new PointCoulombCalculator();
        PointCoulombResult twenty = calculator.calculate(
                PublishedProblems.kaonicSulfur32Legacy3d(constants, 20, 6),
                constants, CalculationMonitor.NONE);
        PointCoulombResult forty = calculator.calculate(
                PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10),
                constants, CalculationMonitor.NONE);
        PointCoulombResult sixty = calculator.calculate(
                PublishedProblems.kaonicSulfur32Legacy3d(constants, 60, 10),
                constants, CalculationMonitor.NONE);
        assertTrue(forty.absoluteErrorMeV() < twenty.absoluteErrorMeV());
        assertTrue(sixty.absoluteErrorMeV() < forty.absoluteErrorMeV());
        assertEquals(-0.367829244928, sixty.calculatedEnergyMeV(), 2e-12);
    }
}
