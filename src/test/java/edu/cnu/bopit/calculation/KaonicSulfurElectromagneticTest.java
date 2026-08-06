package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.UniformChargeSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurElectromagneticTest {
    @Test
    void solvesFiniteNucleusWithUehlingAndRetainsSeparateCorrections() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var point = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var problem = new BopitProblem(point.atomicSystem(), point.quantumState(),
                point.waveEquation(), point.grid(), point.solver(),
                new ElectromagneticSpec(new UniformChargeSpec(2.56), true));
        var result = new PointCoulombCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        assertTrue(result.solverResult().converged());
        assertTrue(result.finiteSizeExpectationMeV() > 0.0);
        assertTrue(result.vacuumPolarizationExpectationMeV() < 0.0);
        assertTrue(Double.isFinite(result.calculatedEnergyMeV()));
        assertTrue(result.finiteSizeCorrectionMeV().getNorm() > 0.0);
        assertTrue(result.vacuumPolarizationMeV().getNorm() > 0.0);
    }
}
