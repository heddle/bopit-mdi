package edu.cnu.bopit.grid;

/** Marker for immutable diagnostics produced by a momentum-grid strategy. */
public sealed interface GridDiagnostics permits LegacyGridDiagnostics, AdaptiveGridDiagnostics {
}
