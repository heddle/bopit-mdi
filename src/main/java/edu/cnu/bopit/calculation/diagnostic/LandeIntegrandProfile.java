package edu.cnu.bopit.calculation.diagnostic;

/** Off-diagonal Landé-subtraction terms for one selected collocation momentum. */
public record LandeIntegrandProfile(int selectedIndex, double selectedMomentumFmInverse,
        double[] otherMomentaFmInverse, double[] singularKernelMeVFm3,
        double[] ordinaryIntegrand, double[] subtractionIntegrand,
        double[] regularizedIntegrand, double analyticTermMeVFm,
        double discreteSubtractionSum, double effectiveDiagonalKernel) {
    public LandeIntegrandProfile {
        otherMomentaFmInverse = otherMomentaFmInverse.clone();
        singularKernelMeVFm3 = singularKernelMeVFm3.clone();
        ordinaryIntegrand = ordinaryIntegrand.clone();
        subtractionIntegrand = subtractionIntegrand.clone();
        regularizedIntegrand = regularizedIntegrand.clone();
        int size = otherMomentaFmInverse.length;
        if (size == 0 || singularKernelMeVFm3.length != size
                || ordinaryIntegrand.length != size || subtractionIntegrand.length != size
                || regularizedIntegrand.length != size) {
            throw new IllegalArgumentException("Landé profile arrays must have equal positive length");
        }
    }

    @Override public double[] otherMomentaFmInverse() { return otherMomentaFmInverse.clone(); }
    @Override public double[] singularKernelMeVFm3() { return singularKernelMeVFm3.clone(); }
    @Override public double[] ordinaryIntegrand() { return ordinaryIntegrand.clone(); }
    @Override public double[] subtractionIntegrand() { return subtractionIntegrand.clone(); }
    @Override public double[] regularizedIntegrand() { return regularizedIntegrand.clone(); }
}
