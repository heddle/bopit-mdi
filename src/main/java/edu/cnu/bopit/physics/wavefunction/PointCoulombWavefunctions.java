package edu.cnu.bopit.physics.wavefunction;

/** Numerical momentum/coordinate wavefunctions and the exact Coulomb comparison. */
public record PointCoulombWavefunctions(MomentumWavefunction momentum,
        MomentumWavefunction analyticMomentum,
        CoordinateWavefunction coordinate, CoordinateWavefunction analyticCoordinate,
        WavefunctionDiagnostics diagnostics) {
    public PointCoulombWavefunctions {
        if (momentum == null || analyticMomentum == null || coordinate == null
                || analyticCoordinate == null || diagnostics == null) {
            throw new IllegalArgumentException("all wavefunction artifacts are required");
        }
    }
}
