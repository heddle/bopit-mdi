package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;

/** Symmetric-basis Hamiltonian, Coulomb operator, and numerical build artifacts. */
public record HamiltonianSystem(RealMatrix matrixMeV, RealMatrix coulombOperatorMeV,
        RealMatrix finiteSizeCorrectionMeV, RealMatrix vacuumPolarizationMeV,
        MomentumGrid grid, LandeMatrixDiagnostics landeDiagnostics) {
    public HamiltonianSystem {
        if (matrixMeV == null || coulombOperatorMeV == null
                || finiteSizeCorrectionMeV == null || vacuumPolarizationMeV == null) {
            throw new IllegalArgumentException("matrix artifacts are required");
        }
        matrixMeV = matrixMeV.copy();
        coulombOperatorMeV = coulombOperatorMeV.copy();
        finiteSizeCorrectionMeV = finiteSizeCorrectionMeV.copy();
        vacuumPolarizationMeV = vacuumPolarizationMeV.copy();
        if (grid == null || landeDiagnostics == null) throw new IllegalArgumentException("metadata is required");
    }
    @Override public RealMatrix matrixMeV() { return matrixMeV.copy(); }
    @Override public RealMatrix coulombOperatorMeV() { return coulombOperatorMeV.copy(); }
    @Override public RealMatrix finiteSizeCorrectionMeV() { return finiteSizeCorrectionMeV.copy(); }
    @Override public RealMatrix vacuumPolarizationMeV() { return vacuumPolarizationMeV.copy(); }
}
