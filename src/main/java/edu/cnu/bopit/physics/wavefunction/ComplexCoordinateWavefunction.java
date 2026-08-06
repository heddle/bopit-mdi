package edu.cnu.bopit.physics.wavefunction;

import org.apache.commons.math3.complex.Complex;

/** Complex radial coordinate-space wavefunction sampled in fm. */
public record ComplexCoordinateWavefunction(double[] radiiFm,
        Complex[] radialValuesFmMinusThreeHalves) {
    public ComplexCoordinateWavefunction {
        radiiFm = radiiFm.clone();
        radialValuesFmMinusThreeHalves = radialValuesFmMinusThreeHalves.clone();
        if (radiiFm.length < 2 || radiiFm.length != radialValuesFmMinusThreeHalves.length) {
            throw new IllegalArgumentException("coordinate arrays must have equal length >= 2");
        }
    }
    @Override public double[] radiiFm() { return radiiFm.clone(); }
    @Override public Complex[] radialValuesFmMinusThreeHalves() {
        return radialValuesFmMinusThreeHalves.clone();
    }
}
