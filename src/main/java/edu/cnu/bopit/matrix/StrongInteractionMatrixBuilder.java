package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.strong.StrongInteractionModel;

/** Builds a complex strong-interaction operator in the symmetric quadrature basis. */
public final class StrongInteractionMatrixBuilder {
    public ComplexMatrix build(int orbitalL, MomentumGrid grid, StrongInteractionModel model) {
        int n = grid.size();
        var real = new Array2DRowRealMatrix(n, n);
        var imaginary = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            double p = grid.point(i);
            for (int j = i; j < n; j++) {
                double pp = grid.point(j);
                var raw = model.partialWaveValue(orbitalL, p, pp);
                double basis = p * Math.sqrt(grid.weight(i)) * pp * Math.sqrt(grid.weight(j));
                real.setEntry(i, j, basis * raw.getReal());
                real.setEntry(j, i, basis * raw.getReal());
                imaginary.setEntry(i, j, basis * raw.getImaginary());
                imaginary.setEntry(j, i, basis * raw.getImaginary());
            }
        }
        return new ComplexMatrix(real, imaginary);
    }
}
