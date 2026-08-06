package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.grid.AdaptiveGridDiagnostics;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurAdaptiveGridRegressionTest {
    @Test
    void solvesBopit1990AutomaticGridCase() {
        var constants = PublishedConstantSets.BOPIT_1990;
        PointCoulombResult adaptive = new PointCoulombCalculator().calculate(
                PublishedProblems.kaonicSulfur32Adaptive3d(constants, 100, 20),
                constants, CalculationMonitor.NONE);
        assertTrue(adaptive.solverResult().converged());
        assertEquals(-0.367831771337, adaptive.calculatedEnergyMeV(), 2e-12);
        assertEquals(4.06412e-6, adaptive.relativeError(), 2e-11);
        assertTrue(adaptive.grid().diagnostics() instanceof AdaptiveGridDiagnostics);
    }

    @Test
    void improvesOnFortyPointLegacyGrid() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var calculator = new PointCoulombCalculator();
        PointCoulombResult legacy = calculator.calculate(
                PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10),
                constants, CalculationMonitor.NONE);
        PointCoulombResult adaptive = calculator.calculate(
                PublishedProblems.kaonicSulfur32Adaptive3d(constants, 100, 20),
                constants, CalculationMonitor.NONE);
        assertTrue(adaptive.absoluteErrorMeV() < legacy.absoluteErrorMeV());
    }
}
