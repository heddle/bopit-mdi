package edu.cnu.bopit.model;

/** Minimal immutable specification for point-Coulomb calculations. */
public record BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
        SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver) {
    public BopitProblem {
        if (atomicSystem == null || quantumState == null || waveEquation == null
                || grid == null || solver == null) {
            throw new IllegalArgumentException("all problem components are required");
        }
    }
}
