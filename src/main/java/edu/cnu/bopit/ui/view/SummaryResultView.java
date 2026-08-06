package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.grid.AdaptiveGridDiagnostics;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.solver.IterationRecord;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Retained textual summary of one completed point-Coulomb calculation. */
public final class SummaryResultView extends BaseView {
    public SummaryResultView(BopitProblem problem, PointCoulombResult result,
            double elapsedSeconds) {
        super(PropertyUtils.TITLE, title(problem),
                PropertyUtils.WIDTH, 650,
                PropertyUtils.HEIGHT, 520,
                PropertyUtils.USECONTAINER, false);
        JTextArea summary = new JTextArea(format(problem, result, elapsedSeconds));
        summary.setEditable(false);
        summary.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        summary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(summary), BorderLayout.CENTER);
    }

    private static String title(BopitProblem problem) {
        return "Result: Z=" + problem.atomicSystem().nuclearCharge()
                + " " + problem.quantumState().principalN()
                + orbitalLetter(problem.quantumState().orbitalL());
    }

    private static String format(BopitProblem problem, PointCoulombResult result,
            double elapsedSeconds) {
        var solver = result.solverResult();
        IterationRecord last = solver.history().get(solver.history().size() - 1);
        StringBuilder text = new StringBuilder(800);
        text.append("POINT-COULOMB SCHRÖDINGER RESULT\n\n");
        text.append(String.format(Locale.US, "Atom/state        Z=%d, A=%d, K-, %d%s%n",
                problem.atomicSystem().nuclearCharge(), problem.atomicSystem().massNumber(),
                problem.quantumState().principalN(), orbitalLetter(problem.quantumState().orbitalL())));
        text.append("Equation          Nonrelativistic Schrödinger\n");
        text.append("Grid              ").append(problem.grid().getClass().getSimpleName())
                .append(", ").append(result.grid().size()).append(" points\n");
        text.append(String.format(Locale.US, "Reduced mass      %.9f MeV%n", result.reducedMassMeV()));
        text.append(String.format(Locale.US, "Calculated energy %.12f MeV%n", result.calculatedEnergyMeV()));
        text.append(String.format(Locale.US, "Exact Coulomb     %.12f MeV%n", result.referenceEnergyMeV()));
        text.append(String.format(Locale.US, "Absolute error    %.6e MeV%n", result.absoluteErrorMeV()));
        text.append(String.format(Locale.US, "Relative error    %.6e (%.6e %%)%n",
                result.relativeError(), 100.0 * result.relativeError()));
        text.append(String.format(Locale.US, "Iterations        %d%n", solver.history().size()));
        text.append(String.format(Locale.US, "Residual norm     %.6e%n", last.residualNorm()));
        text.append("Converged         ").append(solver.converged()).append('\n');
        text.append("Termination       ").append(solver.terminationReason()).append('\n');
        text.append(String.format(Locale.US, "Elapsed           %.3f s%n", elapsedSeconds));
        var wavefunction = result.wavefunctions().diagnostics();
        text.append(String.format(Locale.US, "Momentum norm     %.9f%n", wavefunction.momentumNorm()));
        text.append(String.format(Locale.US, "Coordinate norm   %.9f%n", wavefunction.coordinateNorm()));
        text.append(String.format(Locale.US, "Analytic overlap  %.9f (momentum), %.9f (coordinate)%n",
                wavefunction.absoluteAnalyticMomentumOverlap(),
                wavefunction.absoluteAnalyticCoordinateOverlap()));
        if (result.grid().diagnostics() instanceof AdaptiveGridDiagnostics adaptive
                && !adaptive.warnings().isEmpty()) {
            text.append("\nGRID WARNINGS\n");
            adaptive.warnings().forEach(warning -> text.append("- ").append(warning).append('\n'));
        }
        return text.toString();
    }

    private static String orbitalLetter(int orbitalL) {
        return orbitalL >= 0 && orbitalL < 4 ? String.valueOf("spdf".charAt(orbitalL)) : "(l=" + orbitalL + ")";
    }
}
