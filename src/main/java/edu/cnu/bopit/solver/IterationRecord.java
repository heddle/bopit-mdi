package edu.cnu.bopit.solver;

/** Diagnostics captured after one inverse-iteration solve. */
public record IterationRecord(int iteration, double eigenvalueMeV,
        double historicalEstimateMeV, double energyChangeMeV,
        double residualNorm, double normalizationComponent) {
}
