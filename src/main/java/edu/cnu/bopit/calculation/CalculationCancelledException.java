package edu.cnu.bopit.calculation;

/** Signals cooperative cancellation at a numerical safe point. */
public final class CalculationCancelledException extends RuntimeException {
    public CalculationCancelledException() {
        super("calculation cancelled");
    }
}
