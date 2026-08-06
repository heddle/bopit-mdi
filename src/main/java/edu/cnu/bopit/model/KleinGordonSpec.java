package edu.cnu.bopit.model;

/** Klein-Gordon equation with an explicitly named nuclear-potential coupling. */
public record KleinGordonSpec(KleinGordonForm form, double outerToleranceMeV,
        int maximumOuterIterations) implements WaveEquationSpec {
    public KleinGordonSpec {
        if (form == null || !Double.isFinite(outerToleranceMeV) || outerToleranceMeV <= 0.0
                || maximumOuterIterations < 1) {
            throw new IllegalArgumentException("invalid Klein-Gordon specification");
        }
    }
}
