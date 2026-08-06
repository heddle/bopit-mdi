package edu.cnu.bopit.physics.wavefunction;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;

/** Produces numerical and exact wavefunctions plus normalization diagnostics. */
public final class PointCoulombWavefunctionFactory {
    private static final int RADIAL_INTERVALS = 600;

    private PointCoulombWavefunctionFactory() { }

    public static PointCoulombWavefunctions create(AtomicSystem atom, QuantumState state,
            PhysicalConstantSet constants, MomentumGrid grid, double[] symmetricEigenvector) {
        if (atom == null || state == null || constants == null) {
            throw new IllegalArgumentException("atom, state, and constants are required");
        }
        MomentumWavefunction momentum = MomentumWavefunctionFactory.create(grid, symmetricEigenvector);
        MomentumWavefunction analyticMomentum = HydrogenicMomentumWavefunctionFactory.create(
                atom, state, constants, grid);
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double bohrRadius = constants.hbarCMeVFm()
                / (reducedMass * atom.nuclearCharge() * constants.fineStructureConstant());
        double maximumRadius = 15.0 * state.principalN() * bohrRadius;
        double[] radii = new double[RADIAL_INTERVALS + 1];
        for (int i = 0; i < radii.length; i++) {
            radii[i] = maximumRadius * i / RADIAL_INTERVALS;
        }
        CoordinateWavefunction coordinate = FourierBesselTransform.transform(
                state.orbitalL(), momentum, radii);
        double[] analyticValues = new double[radii.length];
        for (int i = 0; i < radii.length; i++) {
            analyticValues[i] = HydrogenicRadialWavefunction.value(state, bohrRadius, radii[i]);
        }
        CoordinateWavefunction analytic = new CoordinateWavefunction(radii, analyticValues);
        double coordinateNorm = radialNorm(coordinate);
        double analyticNorm = radialNorm(analytic);
        double overlap = radialOverlap(coordinate, analytic);
        double sign = overlap < 0.0 ? -1.0 : 1.0;
        double errorSquared = radialDifferenceSquared(coordinate, analytic, sign);
        double momentumOverlap = momentumOverlap(momentum, analyticMomentum);
        double momentumError = momentumDifference(momentum, analyticMomentum,
                momentumOverlap < 0.0 ? -1.0 : 1.0);
        WavefunctionDiagnostics diagnostics = new WavefunctionDiagnostics(
                momentum.normAfterNormalization(), Math.abs(momentumOverlap), momentumError,
                coordinateNorm, analyticNorm, Math.abs(overlap),
                Math.sqrt(errorSquared) / analyticNorm);
        return new PointCoulombWavefunctions(momentum, analyticMomentum,
                coordinate, analytic, diagnostics);
    }

    static double radialNorm(CoordinateWavefunction wavefunction) {
        return Math.sqrt(integrate(wavefunction.radiiFm(), wavefunction.radialValuesFmMinusThreeHalves(),
                wavefunction.radialValuesFmMinusThreeHalves(), 1.0));
    }

    private static double radialOverlap(CoordinateWavefunction first, CoordinateWavefunction second) {
        return integrate(first.radiiFm(), first.radialValuesFmMinusThreeHalves(),
                second.radialValuesFmMinusThreeHalves(), 1.0);
    }

    private static double momentumOverlap(MomentumWavefunction first,
            MomentumWavefunction second) {
        double[] p = first.momentaFmInverse();
        double[] w = first.weightsFmInverse();
        double[] a = first.radialValuesFmThreeHalves();
        double[] b = second.radialValuesFmThreeHalves();
        double sum = 0.0;
        for (int i = 0; i < p.length; i++) sum += w[i] * p[i] * p[i] * a[i] * b[i];
        return sum;
    }

    private static double momentumDifference(MomentumWavefunction numerical,
            MomentumWavefunction analytic, double analyticSign) {
        double[] p = numerical.momentaFmInverse();
        double[] w = numerical.weightsFmInverse();
        double[] n = numerical.radialValuesFmThreeHalves();
        double[] a = analytic.radialValuesFmThreeHalves();
        double sum = 0.0;
        for (int i = 0; i < p.length; i++) {
            sum += w[i] * p[i] * p[i] * square(n[i] - analyticSign * a[i]);
        }
        return Math.sqrt(sum);
    }

    private static double radialDifferenceSquared(CoordinateWavefunction numerical,
            CoordinateWavefunction analytic, double analyticSign) {
        double[] r = numerical.radiiFm();
        double[] n = numerical.radialValuesFmMinusThreeHalves();
        double[] a = analytic.radialValuesFmMinusThreeHalves();
        double sum = 0.0;
        for (int i = 1; i < r.length; i++) {
            double f0 = r[i - 1] * r[i - 1] * square(n[i - 1] - analyticSign * a[i - 1]);
            double f1 = r[i] * r[i] * square(n[i] - analyticSign * a[i]);
            sum += 0.5 * (r[i] - r[i - 1]) * (f0 + f1);
        }
        return sum;
    }

    private static double integrate(double[] r, double[] first, double[] second, double scale) {
        double sum = 0.0;
        for (int i = 1; i < r.length; i++) {
            double f0 = r[i - 1] * r[i - 1] * first[i - 1] * second[i - 1] * scale;
            double f1 = r[i] * r[i] * first[i] * second[i] * scale;
            sum += 0.5 * (r[i] - r[i - 1]) * (f0 + f1);
        }
        return sum;
    }

    private static double square(double value) {
        return value * value;
    }
}
