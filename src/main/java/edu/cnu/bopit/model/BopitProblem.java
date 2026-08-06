package edu.cnu.bopit.model;

/** Minimal immutable specification for the Stage 1 calculation. */
public record BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
        SchrodingerSpec waveEquation, LegacyGridSpec grid, InverseIterationSpec solver) {
    public BopitProblem {
        if (atomicSystem == null || quantumState == null || waveEquation == null
                || grid == null || solver == null) {
            throw new IllegalArgumentException("all problem components are required");
        }
    }
}
