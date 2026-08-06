package edu.cnu.bopit.physics.wavefunction;

/** Real normalized radial momentum wavefunction on its quadrature grid. */
public record MomentumWavefunction(double[] momentaFmInverse, double[] weightsFmInverse,
        double[] radialValuesFmThreeHalves, double normBeforeNormalization,
        double normAfterNormalization) {
    public MomentumWavefunction {
        momentaFmInverse = momentaFmInverse.clone();
        weightsFmInverse = weightsFmInverse.clone();
        radialValuesFmThreeHalves = radialValuesFmThreeHalves.clone();
        int size = momentaFmInverse.length;
        if (size == 0 || weightsFmInverse.length != size || radialValuesFmThreeHalves.length != size) {
            throw new IllegalArgumentException("momentum-wavefunction arrays must have equal positive length");
        }
    }

    @Override public double[] momentaFmInverse() { return momentaFmInverse.clone(); }
    @Override public double[] weightsFmInverse() { return weightsFmInverse.clone(); }
    @Override public double[] radialValuesFmThreeHalves() { return radialValuesFmThreeHalves.clone(); }
}
