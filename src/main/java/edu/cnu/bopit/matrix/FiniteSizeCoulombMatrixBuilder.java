package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.GaussLegendreRule;
import edu.cnu.bopit.grid.GaussLegendreRules;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.coulomb.LegendreP;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistribution;

/** Builds the regular finite-size correction V[F]-V[1] in the symmetric basis. */
public final class FiniteSizeCoulombMatrixBuilder {
    private static final int ANGULAR_ORDER = 64;

    public RealMatrix buildCorrection(int orbitalL, MomentumGrid grid,
            ChargeDistribution charge, int nuclearCharge, double alpha, double hbarCMeVFm) {
        if (charge.isPointCharge()) return new Array2DRowRealMatrix(grid.size(), grid.size());
        GaussLegendreRule angular = new GaussLegendreRules().create(ANGULAR_ORDER);
        double coefficient = -nuclearCharge * alpha * hbarCMeVFm / Math.PI;
        int n = grid.size();
        RealMatrix result = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            double p = grid.point(i);
            for (int j = i; j < n; j++) {
                double pp = grid.point(j);
                double integral = 0.0;
                for (int k = 0; k < angular.size(); k++) {
                    double x = angular.node(k);
                    double q2 = p * p + pp * pp - 2.0 * p * pp * x;
                    double ratio = finiteDifferenceRatio(charge, Math.sqrt(Math.max(0.0, q2)), q2);
                    integral += angular.weight(k) * LegendreP.value(orbitalL, x) * ratio;
                }
                double raw = coefficient * integral;
                double transformed = p * Math.sqrt(grid.weight(i)) * raw
                        * pp * Math.sqrt(grid.weight(j));
                result.setEntry(i, j, transformed);
                result.setEntry(j, i, transformed);
            }
        }
        return result;
    }

    static double finiteDifferenceRatio(ChargeDistribution charge, double q, double q2) {
        if (q2 < 1e-12) return -charge.rmsRadiusFm() * charge.rmsRadiusFm() / 6.0;
        return (charge.formFactor(q) - 1.0) / q2;
    }
}
