package edu.cnu.bopit.calculation;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.matrix.LandeMatrixDiagnostics;
import edu.cnu.bopit.solver.InverseIterationResult;
import edu.cnu.bopit.physics.wavefunction.PointCoulombWavefunctions;

/** Completed Stage 1 point-Coulomb calculation. */
public record PointCoulombResult(double reducedMassMeV, double referenceEnergyMeV,
        double calculatedEnergyMeV, double absoluteErrorMeV, double relativeError,
        MomentumGrid grid, LandeMatrixDiagnostics landeDiagnostics,
        InverseIterationResult solverResult, RealMatrix hamiltonianMeV,
        RealMatrix coulombOperatorMeV, RealMatrix finiteSizeCorrectionMeV,
        RealMatrix vacuumPolarizationMeV, double finiteSizeExpectationMeV,
        double vacuumPolarizationExpectationMeV, PointCoulombWavefunctions wavefunctions) {
    public PointCoulombResult {
        if (grid == null || landeDiagnostics == null || solverResult == null
                || hamiltonianMeV == null || coulombOperatorMeV == null
                || finiteSizeCorrectionMeV == null || vacuumPolarizationMeV == null
                || wavefunctions == null) {
            throw new IllegalArgumentException("result artifacts are required");
        }
        hamiltonianMeV = hamiltonianMeV.copy();
        coulombOperatorMeV = coulombOperatorMeV.copy();
        finiteSizeCorrectionMeV = finiteSizeCorrectionMeV.copy();
        vacuumPolarizationMeV = vacuumPolarizationMeV.copy();
    }

    @Override public RealMatrix hamiltonianMeV() { return hamiltonianMeV.copy(); }
    @Override public RealMatrix coulombOperatorMeV() { return coulombOperatorMeV.copy(); }
    @Override public RealMatrix finiteSizeCorrectionMeV() { return finiteSizeCorrectionMeV.copy(); }
    @Override public RealMatrix vacuumPolarizationMeV() { return vacuumPolarizationMeV.copy(); }
}
