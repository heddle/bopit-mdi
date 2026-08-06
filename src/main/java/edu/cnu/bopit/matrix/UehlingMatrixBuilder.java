package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.GaussLegendreRule;
import edu.cnu.bopit.grid.GaussLegendreRules;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.LegendreP;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistribution;

/** First-order (Uehling) vacuum-polarization partial-wave operator. */
public final class UehlingMatrixBuilder {
    private static final int ANGULAR_ORDER = 96;
    private static final int SPECTRAL_HALF_ORDER = 40;

    public RealMatrix build(int orbitalL, MomentumGrid grid, ChargeDistribution charge,
            int nuclearCharge, PhysicalConstantSet constants) {
        GaussLegendreRule angular = new GaussLegendreRules().create(ANGULAR_ORDER);
        SpectralRule spectral = spectralRule();
        double coefficient = -nuclearCharge * constants.fineStructureConstant()
                * constants.hbarCMeVFm() / Math.PI;
        double electronComptonFm = constants.hbarCMeVFm() / constants.electronMassMeV();
        int n = grid.size();
        RealMatrix result = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            double p = grid.point(i);
            for (int j = i; j < n; j++) {
                double pp = grid.point(j);
                double angularIntegral = 0.0;
                for (int k = 0; k < angular.size(); k++) {
                    double x = angular.node(k);
                    double q2 = Math.max(0.0, p * p + pp * pp - 2.0 * p * pp * x);
                    double spectralIntegral = 0.0;
                    for (int s = 0; s < spectral.t.length; s++) {
                        double t = spectral.t[s];
                        double mass2 = Math.pow(2.0 * t / electronComptonFm, 2);
                        spectralIntegral += spectral.weight[s] * uehlingWeight(t,
                                constants.fineStructureConstant()) / (q2 + mass2);
                    }
                    angularIntegral += angular.weight(k) * LegendreP.value(orbitalL, x)
                            * charge.formFactor(Math.sqrt(q2)) * spectralIntegral;
                }
                double raw = coefficient * angularIntegral;
                double transformed = p * Math.sqrt(grid.weight(i)) * raw
                        * pp * Math.sqrt(grid.weight(j));
                result.setEntry(i, j, transformed);
                result.setEntry(j, i, transformed);
            }
        }
        return result;
    }

    /** F_2(t) from Kwon and Tabakin, Phys. Rev. C 18 (1978), Eq. (20). */
    public static double uehlingWeight(double t, double alpha) {
        if (!Double.isFinite(t) || t < 1.0 || !Double.isFinite(alpha) || alpha <= 0.0) {
            throw new IllegalArgumentException("t >= 1 and positive alpha are required");
        }
        double t2 = t * t;
        return alpha / Math.PI * (2.0 / (3.0 * t2) + 1.0 / (3.0 * t2 * t2))
                * Math.sqrt(t2 - 1.0);
    }

    private static SpectralRule spectralRule() {
        GaussLegendreRule base = new GaussLegendreRules().create(SPECTRAL_HALF_ORDER);
        double[] t = new double[2 * SPECTRAL_HALF_ORDER];
        double[] weight = new double[t.length];
        for (int i = 0; i < SPECTRAL_HALF_ORDER; i++) {
            double y = base.node(i);
            t[i] = y + 2.0;
            weight[i] = base.weight(i);
            double angle = Math.PI * (3.0 + y) / 8.0;
            double cosine = Math.cos(angle);
            t[i + SPECTRAL_HALF_ORDER] = 3.0 * Math.tan(angle);
            weight[i + SPECTRAL_HALF_ORDER] = 3.0 * Math.PI * base.weight(i)
                    / (8.0 * cosine * cosine);
        }
        return new SpectralRule(t, weight);
    }

    private record SpectralRule(double[] t, double[] weight) { }
}
