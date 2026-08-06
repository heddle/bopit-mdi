package edu.cnu.bopit.calculation;

import java.util.List;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.wavefunction.PointCoulombWavefunctions;
import edu.cnu.bopit.solver.InverseIterationResult;

/** Real point/electromagnetic Klein-Gordon result with nested convergence history. */
public record KleinGordonResult(boolean converged, double bindingEnergyMeV,
        double exactPointCoulombEnergyMeV, MomentumGrid grid,
        List<KleinGordonOuterRecord> outerHistory, InverseIterationResult finalInnerResult,
        PointCoulombWavefunctions wavefunctions, String terminationReason) {
    public KleinGordonResult {
        if (grid == null || outerHistory == null || outerHistory.isEmpty()
                || finalInnerResult == null || wavefunctions == null || terminationReason == null) {
            throw new IllegalArgumentException("Klein-Gordon result artifacts are required");
        }
        outerHistory = List.copyOf(outerHistory);
    }
}
