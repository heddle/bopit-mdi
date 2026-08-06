package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;

/** Landé-regularized Coulomb operator in the symmetric quadrature basis. */
public record CoulombMatrix(RealMatrix operatorMeV, LandeMatrixDiagnostics diagnostics) {
    public CoulombMatrix {
        operatorMeV = operatorMeV.copy();
        if (diagnostics == null) throw new IllegalArgumentException("diagnostics are required");
    }
    @Override public RealMatrix operatorMeV() { return operatorMeV.copy(); }
}
