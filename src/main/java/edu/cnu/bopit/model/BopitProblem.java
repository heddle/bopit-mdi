package edu.cnu.bopit.model;

/** Minimal immutable specification for point-Coulomb calculations. */
public record BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
        SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver,
        ElectromagneticSpec electromagnetic, StrongInteractionSpec strongInteraction) {
    /** Backward-compatible point-Coulomb problem constructor. */
    public BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
            SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver) {
        this(atomicSystem, quantumState, waveEquation, grid, solver,
                ElectromagneticSpec.POINT_COULOMB, new NoStrongInteractionSpec());
    }

    /** Electromagnetic problem constructor with no strong interaction. */
    public BopitProblem(AtomicSystem atomicSystem, QuantumState quantumState,
            SchrodingerSpec waveEquation, GridSpec grid, InverseIterationSpec solver,
            ElectromagneticSpec electromagnetic) {
        this(atomicSystem, quantumState, waveEquation, grid, solver,
                electromagnetic, new NoStrongInteractionSpec());
    }

    public BopitProblem {
        if (atomicSystem == null || quantumState == null || waveEquation == null
                || grid == null || solver == null || electromagnetic == null
                || strongInteraction == null) {
            throw new IllegalArgumentException("all problem components are required");
        }
    }
}
