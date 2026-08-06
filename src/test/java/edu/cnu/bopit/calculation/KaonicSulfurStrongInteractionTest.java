package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class KaonicSulfurStrongInteractionTest {
    @Test
    void complexSulfurCalculationHasPositiveShiftAndWidth() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var density = new FermiChargeSpec(3.20, 0.59, 0.0);
        var problem = new BopitProblem(base.atomicSystem(), base.quantumState(),
                base.waveEquation(), base.grid(), base.solver(), base.electromagnetic(),
                new KwonTabakinOpticalPotentialSpec(new Complex(-0.44, 0.81), density));
        var solver = new ComplexInverseIterationSpec(new Complex(-0.367, -0.001),
                1e-11, 1e-9, 5, 80);
        var result = new StrongInteractionCalculator().calculate(
                problem, constants, solver, CalculationMonitor.NONE);
        assertTrue(result.solverResult().converged());
        assertTrue(result.strongInteractionShiftMeV() > 0.0);
        assertTrue(result.widthMeV() > 0.0);
        assertEquals(-2.0 * result.complexBindingEnergyMeV().getImaginary(), result.widthMeV());
        assertEquals(1.0, result.wavefunctions().momentum().cNormAfterNormalization().getReal(), 1e-10);
        assertEquals(0.0, result.wavefunctions().momentum().cNormAfterNormalization().getImaginary(), 1e-10);
    }
}
