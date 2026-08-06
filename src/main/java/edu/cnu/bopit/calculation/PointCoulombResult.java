package edu.cnu.bopit.calculation;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.LandeMatrixDiagnostics;
import edu.cnu.bopit.solver.InverseIterationResult;

/** Completed Stage 1 point-Coulomb calculation. */
public record PointCoulombResult(double reducedMassMeV, double referenceEnergyMeV,
        double calculatedEnergyMeV, double absoluteErrorMeV, double relativeError,
        MomentumGrid grid, LandeMatrixDiagnostics landeDiagnostics,
        InverseIterationResult solverResult) {
    public PointCoulombResult {
        if (grid == null || landeDiagnostics == null || solverResult == null) {
            throw new IllegalArgumentException("result artifacts are required");
        }
    }
}
