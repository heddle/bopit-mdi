package edu.cnu.bopit.calculation;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.physics.wavefunction.ComplexWavefunctions;
import edu.cnu.bopit.solver.ComplexInverseIterationResult;

/** Complex Klein-Gordon binding energy, shift, width, and nested histories. */
public record ComplexKleinGordonResult(boolean converged, Complex bindingEnergyMeV,
        double electromagneticReferenceEnergyMeV, double strongInteractionShiftMeV,
        double widthMeV, MomentumGrid grid, List<ComplexKleinGordonOuterRecord> outerHistory,
        ComplexInverseIterationResult finalInnerResult, ComplexWavefunctions wavefunctions,
        String terminationReason) {
    public ComplexKleinGordonResult {
        if (bindingEnergyMeV == null || grid == null || outerHistory == null || outerHistory.isEmpty()
                || finalInnerResult == null || wavefunctions == null || terminationReason == null) {
            throw new IllegalArgumentException("complex Klein-Gordon artifacts are required");
        }
        outerHistory = List.copyOf(outerHistory);
    }
}
