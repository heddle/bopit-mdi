package edu.cnu.bopit.calculation;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.solver.InverseIterationResult;

/** Coupled-component point/electromagnetic Dirac result. */
public record DiracResult(double bindingEnergyMeV, double exactPointCoulombEnergyMeV,
        double absoluteErrorMeV, MomentumGrid grid, InverseIterationResult solverResult,
        double largeComponentNorm, double smallComponentNorm) {
    public DiracResult {
        if (grid == null || solverResult == null) throw new IllegalArgumentException("Dirac artifacts are required");
    }
}
