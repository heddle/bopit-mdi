package edu.cnu.bopit.physics.wavefunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.model.QuantumState;

class PointCoulombWavefunctionFactoryTest {
    @Test
    void exactOneSValueAtOriginHasStandardNormalization() {
        double bohrRadius = 3.25;
        assertEquals(2.0 / Math.pow(bohrRadius, 1.5),
                HydrogenicRadialWavefunction.value(new QuantumState(1, 0), bohrRadius, 0.0),
                1e-15);
    }

    @Test
    void sulfur3dWavefunctionsAreNormalizedAndMatchAnalyticState() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Adaptive3d(constants, 100, 10);
        var result = new PointCoulombCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        var diagnostics = result.wavefunctions().diagnostics();
        assertEquals(1.0, diagnostics.momentumNorm(), 2e-14);
        assertTrue(diagnostics.absoluteAnalyticMomentumOverlap() > 0.999999);
        assertTrue(diagnostics.signAlignedMomentumRelativeL2Error() < 5e-4);
        assertEquals(1.0, diagnostics.coordinateNorm(), 2e-3);
        assertEquals(1.0, diagnostics.analyticCoordinateNorm(), 2e-6);
        assertTrue(diagnostics.absoluteAnalyticCoordinateOverlap() > 0.9999);
        assertTrue(diagnostics.signAlignedCoordinateRelativeL2Error() < 0.05);
    }
}
