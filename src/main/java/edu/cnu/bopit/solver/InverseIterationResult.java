package edu.cnu.bopit.solver;

import java.util.List;

/** Immutable selected eigenpair and convergence history. */
public record InverseIterationResult(boolean converged, double eigenvalueMeV,
        double[] eigenvector, List<IterationRecord> history, String terminationReason) {
    public InverseIterationResult {
        eigenvector = eigenvector.clone();
        history = List.copyOf(history);
    }
    @Override public double[] eigenvector() { return eigenvector.clone(); }
}
