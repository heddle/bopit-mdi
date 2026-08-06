package edu.cnu.bopit.persistence;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import edu.cnu.bopit.BopitVersion;
import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.GaussianChargeSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.PointChargeSpec;
import edu.cnu.bopit.model.UniformChargeSpec;

/** Plain-text, human-readable report for a completed point-Coulomb calculation. */
public final class CalculationReport {
    private CalculationReport() { }

    public static void write(BopitProblem problem, PointCoulombResult result, Path path)
            throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            write(problem, result, writer);
        }
    }

    public static void write(BopitProblem p, PointCoulombResult r, Writer out) throws IOException {
        out.write(BopitVersion.APPLICATION_NAME + " " + BopitVersion.VERSION + " calculation report\n\n");
        line(out, "System", "Z=%d, A=%d, particle=%s".formatted(
                p.atomicSystem().nuclearCharge(), p.atomicSystem().massNumber(),
                p.atomicSystem().particle().symbol()));
        line(out, "Masses", format(p.atomicSystem().particleMassMeV()) + " MeV, "
                + format(p.atomicSystem().nuclearMassMeV()) + " MeV");
        line(out, "State", "n=%d, l=%d".formatted(p.quantumState().principalN(),
                p.quantumState().orbitalL()));
        line(out, "Wave equation", p.waveEquation().getClass().getSimpleName());
        line(out, "Electromagnetic model", charge(p));
        line(out, "Strong interaction", strong(p));
        line(out, "Grid", grid(p));
        line(out, "Inverse-iteration shift", format(p.solver().shiftMeV()) + " MeV");
        line(out, "Energy tolerance", format(p.solver().energyTolerance()) + " MeV");
        line(out, "Residual tolerance", format(p.solver().residualTolerance()));
        out.write("\nResults\n");
        line(out, "Reduced mass", format(r.reducedMassMeV()) + " MeV");
        line(out, "Calculated binding energy", format(r.calculatedEnergyMeV()) + " MeV");
        line(out, "Reference Coulomb energy", format(r.referenceEnergyMeV()) + " MeV");
        line(out, "Absolute error", format(r.absoluteErrorMeV()) + " MeV");
        line(out, "Relative error", format(r.relativeError()));
        line(out, "Finite-size contribution", format(r.finiteSizeExpectationMeV()) + " MeV");
        line(out, "Vacuum-polarization contribution", format(r.vacuumPolarizationExpectationMeV()) + " MeV");
        line(out, "Converged", Boolean.toString(r.solverResult().converged()));
        line(out, "Iterations", Integer.toString(r.solverResult().history().size()));
        double residual = r.solverResult().history().isEmpty() ? Double.NaN
                : r.solverResult().history().get(r.solverResult().history().size() - 1).residualNorm();
        line(out, "Final residual", format(residual));
        line(out, "Termination", r.solverResult().terminationReason());
    }

    private static String grid(BopitProblem p) {
        if (p.grid() instanceof LegacyGridSpec g) return "legacy, total=%d, nuclear=%d, CATOM=%s fm^-1, "
                .formatted(g.totalPoints(), g.nuclearPoints(), format(g.atomicScale()))
                + "CNUCL=%s fm^-1, CSIZE=%s fm^-1, CPMAX=%s".formatted(format(g.nuclearScale()),
                        format(g.regionBoundary()), format(g.maximumMomentumScale()));
        AdaptiveGridSpec g = (AdaptiveGridSpec) p.grid();
        return "adaptive, total=%d, nuclear=%d, maximum=%s fm^-1, region bias=%s".formatted(
                g.totalPoints(), g.nuclearPoints(), format(g.maximumMomentum()), format(g.regionBias()));
    }

    private static String charge(BopitProblem p) {
        var c = p.electromagnetic().nuclearCharge();
        String model = c instanceof PointChargeSpec ? "point"
                : c instanceof UniformChargeSpec u ? "uniform, rms=" + format(u.rmsRadiusFm()) + " fm"
                : c instanceof GaussianChargeSpec g ? "Gaussian, rms=" + format(g.rmsRadiusFm()) + " fm"
                : c instanceof FermiChargeSpec f ? "Fermi, c=" + format(f.halfDensityRadiusFm())
                        + " fm, a=" + format(f.diffusenessFm()) + " fm, w=" + format(f.w()) : c.toString();
        return model + (p.electromagnetic().uehlingVacuumPolarization() ? ", with Uehling" : "");
    }

    private static String strong(BopitProblem p) {
        if (p.strongInteraction() instanceof KwonTabakinOpticalPotentialSpec s) {
            return "Kwon-Tabakin local, fitted scattering length="
                    + format(s.fittedScatteringLengthFm().getReal()) + "+i"
                    + format(s.fittedScatteringLengthFm().getImaginary()) + " fm";
        }
        return "none";
    }

    private static void line(Writer out, String label, String value) throws IOException {
        out.write("%-34s %s%n".formatted(label + ":", value));
    }
    private static String format(double value) { return String.format(Locale.US, "%.16g", value); }
}
