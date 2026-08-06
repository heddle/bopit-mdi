package edu.cnu.bopit.physics.wavefunction;

import org.apache.commons.math3.complex.Complex;

/** Complex radial momentum wavefunction with Gamow c-product normalization. */
public record ComplexMomentumWavefunction(double[] momentaFmInverse,
        double[] weightsFmInverse, Complex[] radialValuesFmThreeHalves,
        Complex cNormBeforeNormalization, Complex cNormAfterNormalization) {
    public ComplexMomentumWavefunction {
        momentaFmInverse = momentaFmInverse.clone();
        weightsFmInverse = weightsFmInverse.clone();
        radialValuesFmThreeHalves = radialValuesFmThreeHalves.clone();
        if (momentaFmInverse.length == 0 || weightsFmInverse.length != momentaFmInverse.length
                || radialValuesFmThreeHalves.length != momentaFmInverse.length
                || cNormBeforeNormalization == null || cNormAfterNormalization == null) {
            throw new IllegalArgumentException("complex momentum-wavefunction artifacts are required");
        }
    }
    @Override public double[] momentaFmInverse() { return momentaFmInverse.clone(); }
    @Override public double[] weightsFmInverse() { return weightsFmInverse.clone(); }
    @Override public Complex[] radialValuesFmThreeHalves() { return radialValuesFmThreeHalves.clone(); }
}
