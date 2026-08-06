package edu.cnu.bopit.solver;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

/** Selected complex eigenpair and convergence diagnostics. */
public record ComplexInverseIterationResult(boolean converged, Complex eigenvalueMeV,
        Complex[] eigenvector, List<ComplexIterationRecord> history, String terminationReason) {
    public ComplexInverseIterationResult {
        if (eigenvalueMeV == null || eigenvector == null || history == null
                || terminationReason == null) throw new IllegalArgumentException("result fields are required");
        eigenvector = eigenvector.clone();
        history = List.copyOf(history);
    }
    @Override public Complex[] eigenvector() { return eigenvector.clone(); }
}
