package edu.cnu.bopit.physics.wavefunction;

/** Real radial coordinate-space wavefunction sampled in femtometres. */
public record CoordinateWavefunction(double[] radiiFm, double[] radialValuesFmMinusThreeHalves) {
    public CoordinateWavefunction {
        radiiFm = radiiFm.clone();
        radialValuesFmMinusThreeHalves = radialValuesFmMinusThreeHalves.clone();
        if (radiiFm.length < 2 || radiiFm.length != radialValuesFmMinusThreeHalves.length) {
            throw new IllegalArgumentException("coordinate-wavefunction arrays must have equal length >= 2");
        }
    }

    @Override public double[] radiiFm() { return radiiFm.clone(); }
    @Override public double[] radialValuesFmMinusThreeHalves() {
        return radialValuesFmMinusThreeHalves.clone();
    }
}
