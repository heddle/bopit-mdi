package edu.cnu.bopit.study;

import java.util.List;

import edu.cnu.bopit.model.BopitProblem;

/** Reusable one-axis study definitions for the workbench. */
public final class NamedStudyTemplates {
    private static final List<StudyObservable> STANDARD = List.of(
            StudyObservable.BINDING_ENERGY_MEV, StudyObservable.ABSOLUTE_ERROR_MEV,
            StudyObservable.STRONG_SHIFT_MEV, StudyObservable.WIDTH_MEV,
            StudyObservable.ITERATIONS, StudyObservable.RESIDUAL);

    private NamedStudyTemplates() { }

    public static ParameterStudy gridConvergence(BopitProblem base) {
        return new ParameterStudy("Grid convergence", base,
                List.of(new ExplicitParameterAxis("Grid points", ParameterTarget.TOTAL_GRID_POINTS,
                        List.of(v("20", 20), v("30", 30), v("40", 40), v("60", 60)))), STANDARD);
    }
    public static ParameterStudy cutoffStability(BopitProblem base) {
        return new ParameterStudy("Cutoff stability", base,
                List.of(new LogarithmicParameterAxis("Maximum momentum",
                        ParameterTarget.MAXIMUM_MOMENTUM_FM_INVERSE, 100.0, 100_000.0, 7)), STANDARD);
    }
    public static ParameterStudy adaptiveVersusLegacy(BopitProblem base) {
        return new ParameterStudy("Adaptive versus legacy", base,
                List.of(new ExplicitParameterAxis("Grid kind", ParameterTarget.GRID_KIND,
                        List.of(v("Legacy", 0), v("Adaptive", 1)))), STANDARD);
    }
    public static ParameterStudy regionBias(BopitProblem base) {
        return new ParameterStudy("Adaptive region bias", base,
                List.of(new LinearParameterAxis("Region bias", ParameterTarget.ADAPTIVE_REGION_BIAS,
                        0.2, 0.8, 7)), STANDARD);
    }
    public static ParameterStudy strongPotentialSensitivity(BopitProblem base) {
        return new ParameterStudy("Strong-potential sensitivity", base,
                List.of(new LinearParameterAxis("Re scattering length",
                        ParameterTarget.STRONG_REAL_SCATTERING_LENGTH_FM, 0.2, 0.7, 6)), STANDARD);
    }
    public static ParameterStudy finiteSizeSensitivity(BopitProblem base) {
        return new ParameterStudy("Finite-size sensitivity", base,
                List.of(new LinearParameterAxis("Charge radius", ParameterTarget.CHARGE_RADIUS_FM,
                        2.0, 4.0, 9)), STANDARD);
    }
    public static ParameterStudy waveEquationComparison(BopitProblem base) {
        return new ParameterStudy("Wave-equation comparison", base,
                List.of(new ExplicitParameterAxis("Wave equation", ParameterTarget.WAVE_EQUATION,
                        List.of(v("Schrodinger", 0), v("Relativistic Schrodinger", 1),
                                v("KG energy weighted", 2), v("KG full vector", 3),
                                v("KG mass weighted", 4), v("KG scalar", 5), v("Dirac", 6)))), STANDARD);
    }

    private static AxisValue v(String label, double value) { return new AxisValue(label, value); }
}
