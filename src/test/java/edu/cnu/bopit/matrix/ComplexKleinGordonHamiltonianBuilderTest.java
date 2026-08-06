package edu.cnu.bopit.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.grid.LegacyGridDiagnostics;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.KleinGordonForm;

class ComplexKleinGordonHamiltonianBuilderTest {
    @Test
    void implementsAllFourNamedPotentialForms() {
        MomentumGrid grid = new MomentumGrid(new double[] {1.0}, new double[] {1.0},
                List.of(), new LegacyGridDiagnostics(1, 0, 1.0, 2.0));
        var coulomb = new Array2DRowRealMatrix(new double[][] {{-2.0}});
        Complex nuclearValue = new Complex(0.3, -0.4);
        var nuclear = new ComplexMatrix(
                new Array2DRowRealMatrix(new double[][] {{nuclearValue.getReal()}}),
                new Array2DRowRealMatrix(new double[][] {{nuclearValue.getImaginary()}}));
        Complex binding = new Complex(-0.2, -0.01);
        double mu = 5.0;
        Complex c = new Complex(-2.0, 0.0);
        Complex total = c.add(nuclearValue);
        var builder = new ComplexKleinGordonHamiltonianBuilder();

        Complex kinetic = Complex.ONE;
        assertEntry(total.multiply(binding.add(mu).multiply(2)).subtract(c.multiply(c)).add(kinetic),
                builder.build(KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR, binding, mu,
                        grid, coulomb, nuclear, 1.0));
        assertEntry(total.multiply(binding.add(mu).multiply(2)).subtract(total.multiply(total)).add(kinetic),
                builder.build(KleinGordonForm.FULL_VECTOR, binding, mu,
                        grid, coulomb, nuclear, 1.0));
        assertEntry(c.multiply(binding.add(mu).multiply(2)).subtract(c.multiply(c))
                        .add(nuclearValue.multiply(2.0 * mu)).add(kinetic),
                builder.build(KleinGordonForm.MASS_WEIGHTED_NUCLEAR, binding, mu,
                        grid, coulomb, nuclear, 1.0));
        assertEntry(c.multiply(binding.add(mu).multiply(2)).subtract(c.multiply(c))
                        .add(nuclearValue.multiply(2.0 * mu)).add(nuclearValue.multiply(nuclearValue))
                        .add(kinetic),
                builder.build(KleinGordonForm.SCALAR_NUCLEAR, binding, mu,
                        grid, coulomb, nuclear, 1.0));
    }

    private static void assertEntry(Complex expected, ComplexMatrix actual) {
        assertEquals(expected.getReal(), actual.entry(0, 0).getReal(), 1e-13);
        assertEquals(expected.getImaginary(), actual.entry(0, 0).getImaginary(), 1e-13);
    }
}
