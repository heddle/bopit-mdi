package edu.cnu.bopit.matrix;

/** Per-grid-point decomposition of the Landé diagonal prescription. */
public record LandeMatrixDiagnostics(double[] analyticTerms,
        double[] discreteSubtractionSums, double[] effectiveDiagonalKernels,
        double symmetryResidual) {
    public LandeMatrixDiagnostics {
        analyticTerms = analyticTerms.clone();
        discreteSubtractionSums = discreteSubtractionSums.clone();
        effectiveDiagonalKernels = effectiveDiagonalKernels.clone();
    }
    @Override public double[] analyticTerms() { return analyticTerms.clone(); }
    @Override public double[] discreteSubtractionSums() { return discreteSubtractionSums.clone(); }
    @Override public double[] effectiveDiagonalKernels() { return effectiveDiagonalKernels.clone(); }
}
