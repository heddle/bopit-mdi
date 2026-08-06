package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;

/** Symmetric-basis Hamiltonian and the numerical artifacts used to build it. */
public record HamiltonianSystem(RealMatrix matrixMeV, MomentumGrid grid,
        LandeMatrixDiagnostics landeDiagnostics) {
    public HamiltonianSystem {
        matrixMeV = matrixMeV.copy();
        if (grid == null || landeDiagnostics == null) throw new IllegalArgumentException("metadata is required");
    }
    @Override public RealMatrix matrixMeV() { return matrixMeV.copy(); }
}
