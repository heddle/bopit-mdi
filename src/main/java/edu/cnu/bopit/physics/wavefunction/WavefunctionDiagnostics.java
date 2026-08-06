package edu.cnu.bopit.physics.wavefunction;

/** Normalization and analytic-comparison diagnostics for a real bound state. */
public record WavefunctionDiagnostics(double momentumNorm,
        double absoluteAnalyticMomentumOverlap, double signAlignedMomentumRelativeL2Error,
        double coordinateNorm, double analyticCoordinateNorm,
        double absoluteAnalyticCoordinateOverlap,
        double signAlignedCoordinateRelativeL2Error) { }
