package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.coulomb.CoulombKernel;
import edu.cnu.bopit.physics.coulomb.LandeIntegral;
import edu.cnu.bopit.physics.coulomb.LegendreP;

/** Builds the finite Coulomb matrix without evaluating its singular diagonal. */
public final class LandeCoulombMatrixBuilder {
    public CoulombMatrix build(int orbitalL, MomentumGrid grid, CoulombKernel kernel,
            int nuclearCharge, double alpha, double hbarCMeVFm) {
        int n = grid.size();
        double[][] raw = new double[n][n];
        double[] analytic = new double[n];
        double[] sums = new double[n];
        double[] diagonal = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double value = kernel.offDiagonal(orbitalL, grid.point(i), grid.point(j));
                raw[i][j] = value;
                raw[j][i] = value;
            }
        }
        for (int i = 0; i < n; i++) {
            double pi = grid.point(i);
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                double pj = grid.point(j);
                double z = 0.5 * (pi / pj + pj / pi);
                sums[i] += raw[i][j] * grid.weight(j) / LegendreP.value(orbitalL, z);
            }
            analytic[i] = LandeIntegral.valueMeVFm(
                    orbitalL, pi, nuclearCharge, alpha, hbarCMeVFm);
            diagonal[i] = (analytic[i] - sums[i]) / grid.weight(i);
            raw[i][i] = diagonal[i];
        }

        RealMatrix transformed = new Array2DRowRealMatrix(n, n);
        double symmetryResidual = 0.0;
        for (int i = 0; i < n; i++) {
            double left = grid.point(i) * Math.sqrt(grid.weight(i));
            for (int j = 0; j < n; j++) {
                double right = grid.point(j) * Math.sqrt(grid.weight(j));
                transformed.setEntry(i, j, left * raw[i][j] * right);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                symmetryResidual = Math.max(symmetryResidual,
                        Math.abs(transformed.getEntry(i, j) - transformed.getEntry(j, i)));
            }
        }
        return new CoulombMatrix(transformed,
                new LandeMatrixDiagnostics(analytic, sums, diagonal, symmetryResidual));
    }
}
