package edu.cnu.bopit.grid;

/** Point allocation and limits for a historical two-region grid. */
public record LegacyGridDiagnostics(int atomicPoints, int nuclearPoints,
        double regionBoundary, double limitingUpperEndpoint) implements GridDiagnostics {
}
