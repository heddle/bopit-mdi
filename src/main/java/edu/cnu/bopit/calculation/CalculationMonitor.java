package edu.cnu.bopit.calculation;

/** Headless progress and cooperative-cancellation boundary. */
public interface CalculationMonitor {
    CalculationMonitor NONE = new CalculationMonitor() { };

    default boolean isCancellationRequested() { return false; }
    default void progress(double fraction, String message) { }

    default void checkCancelled() {
        if (isCancellationRequested()) throw new CalculationCancelledException();
    }
}
