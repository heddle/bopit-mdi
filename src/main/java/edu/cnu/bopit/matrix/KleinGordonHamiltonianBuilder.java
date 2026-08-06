package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;

/** Builds the real electromagnetic Klein-Gordon squared-energy operator. */
public final class KleinGordonHamiltonianBuilder {
    /** p^2 c^2 + 2(mu+E_B)V - V^2, with eigenvalue E_B(E_B+2mu). */
    public RealMatrix build(double bindingEnergyMeV, double reducedMassMeV,
            MomentumGrid grid, RealMatrix electromagneticPotentialMeV) {
        if (!Double.isFinite(bindingEnergyMeV) || !Double.isFinite(reducedMassMeV)
                || reducedMassMeV <= 0.0 || grid == null || electromagneticPotentialMeV == null
                || electromagneticPotentialMeV.getRowDimension() != grid.size()
                || electromagneticPotentialMeV.getColumnDimension() != grid.size()) {
            throw new IllegalArgumentException("invalid Klein-Gordon builder inputs");
        }
        RealMatrix result = electromagneticPotentialMeV.scalarMultiply(
                2.0 * (reducedMassMeV + bindingEnergyMeV));
        result = result.subtract(electromagneticPotentialMeV.multiply(electromagneticPotentialMeV));
        // Momentum is fm^-1; the potential matrices are already in MeV.
        // The caller supplies hbar*c through addKinetic to keep constants explicit.
        return result;
    }

    public void addKinetic(RealMatrix matrix, MomentumGrid grid, double hbarCMeVFm) {
        for (int i = 0; i < grid.size(); i++) {
            double pc = hbarCMeVFm * grid.point(i);
            matrix.addToEntry(i, i, pc * pc);
        }
    }
}
