package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.StrongInteractionResult;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.mdi.view.ViewPropertiesBuilder;
import edu.cnu.mdi.view.BaseView;

/** Retained summary of a complex strong-interaction calculation. */
public final class StrongInteractionSummaryView extends BaseView {
    public StrongInteractionSummaryView(BopitProblem problem, StrongInteractionResult result,
            double elapsedSeconds) {
        super(new ViewPropertiesBuilder().title("Complex Strong-Interaction Result")
                .width(680).height(500).useContainer(false).buildOptions());
        var energy = result.complexBindingEnergyMeV();
        var last = result.solverResult().history().get(result.solverResult().history().size() - 1);
        String text = String.format(Locale.US,
                "COMPLEX SCHRÖDINGER RESULT%n%n"
                + "Atom/state           Z=%d, A=%d, %d%s%n"
                + "Strong model         %s%n"
                + "EM reference         %.12f MeV%n"
                + "Complex energy       %.12f %+.12f i MeV%n"
                + "Strong shift         %.9e MeV  (%.6f keV)%n"
                + "Width Gamma          %.9e MeV  (%.6f keV)%n"
                + "Convention            E = E_R - i Gamma/2%n"
                + "Iterations            %d%n"
                + "Residual norm         %.6e%n"
                + "Converged             %s%n"
                + "Complex c-norm        %s%n"
                + "Elapsed               %.3f s%n",
                problem.atomicSystem().nuclearCharge(), problem.atomicSystem().massNumber(),
                problem.quantumState().principalN(), orbitalLetter(problem.quantumState().orbitalL()),
                problem.strongInteraction().getClass().getSimpleName(),
                result.electromagneticReferenceEnergyMeV(), energy.getReal(), energy.getImaginary(),
                result.strongInteractionShiftMeV(), 1_000.0 * result.strongInteractionShiftMeV(),
                result.widthMeV(), 1_000.0 * result.widthMeV(),
                result.solverResult().history().size(), last.residualNorm(),
                result.solverResult().converged(),
                result.wavefunctions().momentum().cNormAfterNormalization(), elapsedSeconds);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
    }

    private static String orbitalLetter(int l) {
        return l >= 0 && l < 4 ? String.valueOf("spdf".charAt(l)) : "(l=" + l + ")";
    }
}
