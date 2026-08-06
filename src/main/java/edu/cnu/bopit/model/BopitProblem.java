package edu.cnu.bopit.model;

/** Minimal immutable specification for point-Coulomb calculations. */
public record BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
        SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver,
        ElectromagneticSpec electromagnetic) {
    /** Backward-compatible point-Coulomb problem constructor. */
    public BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
            SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver) {
        this(atomicSystem, quantumState, waveEquation, grid, solver,
                ElectromagneticSpec.POINT_COULOMB);
    }

    public BopitProblem {
        if (atomicSystem == null || quantumState == null || waveEquation == null
                || grid == null || solver == null || electromagnetic == null) {
            throw new IllegalArgumentException("all problem components are required");
        }
    }
}
