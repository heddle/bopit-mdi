package edu.cnu.bopit.physics.wavefunction;

/** Normalized complex momentum- and coordinate-space radial wavefunctions. */
public record ComplexWavefunctions(ComplexMomentumWavefunction momentum,
        ComplexCoordinateWavefunction coordinate) {
    public ComplexWavefunctions {
        if (momentum == null || coordinate == null) throw new IllegalArgumentException("wavefunctions are required");
    }
}
