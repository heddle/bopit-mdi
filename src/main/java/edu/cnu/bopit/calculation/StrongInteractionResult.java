package edu.cnu.bopit.calculation;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.ComplexMatrix;
import edu.cnu.bopit.physics.wavefunction.ComplexWavefunctions;
import edu.cnu.bopit.solver.ComplexInverseIterationResult;

/** Result of a complex Schrödinger calculation with a strong interaction. */
public record StrongInteractionResult(double electromagneticReferenceEnergyMeV,
        Complex complexBindingEnergyMeV, double strongInteractionShiftMeV,
        double widthMeV, MomentumGrid grid, ComplexMatrix hamiltonianMeV,
        ComplexMatrix strongOperatorMeV, ComplexInverseIterationResult solverResult,
        ComplexWavefunctions wavefunctions) {
    public StrongInteractionResult {
        if (complexBindingEnergyMeV == null || grid == null || hamiltonianMeV == null
                || strongOperatorMeV == null || solverResult == null || wavefunctions == null) {
            throw new IllegalArgumentException("strong-interaction result artifacts are required");
        }
    }
}
