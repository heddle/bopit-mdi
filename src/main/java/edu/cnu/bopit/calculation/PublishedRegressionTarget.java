package edu.cnu.bopit.calculation;

/** A printed historical observation kept separate from executable constant sets. */
public record PublishedRegressionTarget(String id, String source,
        Double reducedMassMeV, double referenceEnergyMeV,
        Double calculatedEnergyMeV, Integer totalGridPoints) {
    public PublishedRegressionTarget {
        if (id == null || id.isBlank() || source == null || source.isBlank()) {
            throw new IllegalArgumentException("id and source are required");
        }
        if (!Double.isFinite(referenceEnergyMeV)) {
            throw new IllegalArgumentException("referenceEnergyMeV must be finite");
        }
        if (reducedMassMeV != null && (!Double.isFinite(reducedMassMeV) || reducedMassMeV <= 0.0)) {
            throw new IllegalArgumentException("reducedMassMeV must be positive when supplied");
        }
        if (calculatedEnergyMeV != null && !Double.isFinite(calculatedEnergyMeV)) {
            throw new IllegalArgumentException("calculatedEnergyMeV must be finite when supplied");
        }
        if (totalGridPoints != null && totalGridPoints <= 0) {
            throw new IllegalArgumentException("totalGridPoints must be positive when supplied");
        }
    }
}
