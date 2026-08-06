package edu.cnu.bopit.grid;

/** Contiguous region of a momentum grid; end index is exclusive. */
public record GridRegion(Type type, int startIndex, int endIndex,
        double lowerLimit, double upperLimit) {
    public enum Type { ATOMIC, NUCLEAR }

    public GridRegion {
        if (type == null || startIndex < 0 || endIndex <= startIndex) {
            throw new IllegalArgumentException("invalid grid region indices");
        }
        if (!Double.isFinite(lowerLimit) || !Double.isFinite(upperLimit)
                || lowerLimit < 0.0 || upperLimit <= lowerLimit) {
            throw new IllegalArgumentException("invalid grid region limits");
        }
    }
}
