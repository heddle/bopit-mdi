package edu.cnu.bopit.calculation.diagnostic;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.LegendreP;
import edu.cnu.bopit.physics.coulomb.PointCoulombKernel;

/** Builds inspectable Landé terms without evaluating the singular diagonal. */
public final class LandeIntegrandProfileFactory {
    private LandeIntegrandProfileFactory() { }

    public static LandeIntegrandProfile create(BopitProblem problem,
            PointCoulombResult result, PhysicalConstantSet constants, int selectedIndex) {
        if (problem == null || result == null || constants == null) {
            throw new IllegalArgumentException("problem, result, and constants are required");
        }
        MomentumGrid grid = result.grid();
        if (selectedIndex < 0 || selectedIndex >= grid.size()) {
            throw new IllegalArgumentException("selected index is outside the momentum grid");
        }
        int l = problem.quantumState().orbitalL();
        double[] eigenvector = result.solverResult().eigenvector();
        double[] phi = new double[grid.size()];
        for (int i = 0; i < phi.length; i++) {
            phi[i] = eigenvector[i] / (grid.point(i) * Math.sqrt(grid.weight(i)));
        }
        double pi = grid.point(selectedIndex);
        double phiI = phi[selectedIndex];
        int count = grid.size() - 1;
        double[] p = new double[count];
        double[] kernelValues = new double[count];
        double[] ordinary = new double[count];
        double[] subtraction = new double[count];
        double[] regularized = new double[count];
        PointCoulombKernel kernel = new PointCoulombKernel(
                problem.atomicSystem().nuclearCharge(), constants.fineStructureConstant(),
                constants.hbarCMeVFm());
        int out = 0;
        for (int j = 0; j < grid.size(); j++) {
            if (j == selectedIndex) continue;
            double pj = grid.point(j);
            double value = kernel.offDiagonal(l, pi, pj);
            double z = 0.5 * (pi / pj + pj / pi);
            p[out] = pj;
            kernelValues[out] = value;
            ordinary[out] = value * phi[j] * pj * pj;
            subtraction[out] = value * phiI * pi * pi / LegendreP.value(l, z);
            regularized[out] = ordinary[out] - subtraction[out];
            out++;
        }
        var diagnostics = result.landeDiagnostics();
        return new LandeIntegrandProfile(selectedIndex, pi, p, kernelValues,
                ordinary, subtraction, regularized,
                diagnostics.analyticTerms()[selectedIndex],
                diagnostics.discreteSubtractionSums()[selectedIndex],
                diagnostics.effectiveDiagonalKernels()[selectedIndex]);
    }
}
