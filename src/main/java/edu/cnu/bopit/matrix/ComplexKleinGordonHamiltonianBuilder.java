package edu.cnu.bopit.matrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.KleinGordonForm;

/** Complex squared-energy Klein-Gordon operators for the four named nuclear couplings. */
public final class ComplexKleinGordonHamiltonianBuilder {
    public ComplexMatrix build(KleinGordonForm form, Complex bindingEnergyMeV,
            double reducedMassMeV, MomentumGrid grid, RealMatrix coulombMeV,
            ComplexMatrix nuclearMeV, double hbarCMeVFm) {
        if (form == null || bindingEnergyMeV == null || reducedMassMeV <= 0.0
                || grid == null || coulombMeV == null || nuclearMeV == null
                || nuclearMeV.rows() != grid.size() || coulombMeV.getRowDimension() != grid.size()) {
            throw new IllegalArgumentException("invalid complex Klein-Gordon inputs");
        }
        ComplexMatrix c = ComplexMatrixAlgebra.real(coulombMeV);
        ComplexMatrix total = ComplexMatrixAlgebra.add(c, nuclearMeV);
        ComplexMatrix cSquared = ComplexMatrixAlgebra.multiply(c, c);
        Complex twoTotalEnergy = bindingEnergyMeV.add(reducedMassMeV).multiply(2.0);
        ComplexMatrix operator = switch (form) {
            case ENERGY_WEIGHTED_NUCLEAR -> ComplexMatrixAlgebra.subtract(
                    ComplexMatrixAlgebra.scale(total, twoTotalEnergy), cSquared);
            case FULL_VECTOR -> ComplexMatrixAlgebra.subtract(
                    ComplexMatrixAlgebra.scale(total, twoTotalEnergy),
                    ComplexMatrixAlgebra.multiply(total, total));
            case MASS_WEIGHTED_NUCLEAR -> ComplexMatrixAlgebra.add(
                    ComplexMatrixAlgebra.subtract(
                            ComplexMatrixAlgebra.scale(c, twoTotalEnergy), cSquared),
                    ComplexMatrixAlgebra.scale(nuclearMeV, new Complex(2.0 * reducedMassMeV, 0.0)));
            case SCALAR_NUCLEAR -> ComplexMatrixAlgebra.add(
                    ComplexMatrixAlgebra.add(
                            ComplexMatrixAlgebra.subtract(
                                    ComplexMatrixAlgebra.scale(c, twoTotalEnergy), cSquared),
                            ComplexMatrixAlgebra.scale(nuclearMeV,
                                    new Complex(2.0 * reducedMassMeV, 0.0))),
                    ComplexMatrixAlgebra.multiply(nuclearMeV, nuclearMeV));
        };
        double[] kinetic = new double[grid.size()];
        for (int i = 0; i < kinetic.length; i++) {
            double pc = hbarCMeVFm * grid.point(i);
            kinetic[i] = pc * pc;
        }
        return ComplexMatrixAlgebra.addDiagonal(operator, kinetic);
    }
}
