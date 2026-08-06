package edu.cnu.bopit.model;

/** Convergence controls for shifted inverse iteration. */
public record InverseIterationSpec(double shiftMeV, double energyTolerance,
        double residualTolerance, int minimumIterations, int maximumIterations) {
    public InverseIterationSpec {
        if (!Double.isFinite(shiftMeV)) throw new IllegalArgumentException("shiftMeV must be finite");
        requirePositive(energyTolerance, "energyTolerance");
        requirePositive(residualTolerance, "residualTolerance");
        if (minimumIterations < 1) throw new IllegalArgumentException("minimumIterations must be positive");
        if (maximumIterations < minimumIterations) {
            throw new IllegalArgumentException("maximumIterations must be >= minimumIterations");
        }
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }
}
