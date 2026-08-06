package edu.cnu.bopit.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class ComplexKleinGordonCalculatorTest {
    @ParameterizedTest
    @EnumSource(KleinGordonForm.class)
    void eachNamedNuclearCouplingConvergesWithPhysicalWidth(KleinGordonForm form) {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 30, 8);
        var density = new FermiChargeSpec(3.20, 0.59, 0.0);
        var problem = new BopitProblem(base.atomicSystem(), base.quantumState(),
                new KleinGordonSpec(form, 2e-9, 15), base.grid(), base.solver(),
                base.electromagnetic(),
                new KwonTabakinOpticalPotentialSpec(new Complex(0.44, 0.83), density));
        var result = new ComplexKleinGordonCalculator().calculate(
                problem, constants, CalculationMonitor.NONE);
        assertTrue(result.converged(), form.toString());
        assertTrue(result.widthMeV() > 0.0, form.toString());
        assertEquals(-2.0 * result.bindingEnergyMeV().getImaginary(), result.widthMeV());
        assertTrue(result.outerHistory().size() >= 2);
    }
}
