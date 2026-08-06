package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.ComplexKleinGordonResult;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Complex Klein-Gordon shift, width, and nested convergence report. */
public final class ComplexKleinGordonSummaryView extends BaseView {
    public ComplexKleinGordonSummaryView(BopitProblem problem,
            ComplexKleinGordonResult result, double elapsedSeconds) {
        super(PropertyUtils.TITLE, "Complex Klein-Gordon Result",
                PropertyUtils.WIDTH, 740, PropertyUtils.HEIGHT, 560,
                PropertyUtils.USECONTAINER, false);
        var energy = result.bindingEnergyMeV();
        StringBuilder text = new StringBuilder();
        text.append("COMPLEX KLEIN-GORDON RESULT\n\n");
        text.append("Form                 ").append(problem.waveEquation()).append('\n');
        if (problem.strongInteraction() instanceof KwonTabakinOpticalPotentialSpec optical) {
            text.append(String.format(Locale.US,
                    "Fitted a-bar         %.6f %+.6f i fm (per nucleon)%n",
                    optical.fittedScatteringLengthFm().getReal(),
                    optical.fittedScatteringLengthFm().getImaginary()));
        }
        text.append(String.format(Locale.US, "EM reference         %.12f MeV%n",
                result.electromagneticReferenceEnergyMeV()));
        text.append(String.format(Locale.US, "Complex binding      %.12f %+.12f i MeV%n",
                energy.getReal(), energy.getImaginary()));
        text.append(String.format(Locale.US, "Strong shift         %.9e MeV  (%.6f keV)%n",
                result.strongInteractionShiftMeV(), 1_000.0 * result.strongInteractionShiftMeV()));
        text.append(String.format(Locale.US, "Width Gamma          %.9e MeV  (%.6f keV)%n",
                result.widthMeV(), 1_000.0 * result.widthMeV()));
        text.append("Convention            E = E_R - i Gamma/2\n");
        text.append("Converged             ").append(result.converged()).append('\n');
        if (isTableIIISulfur(problem)) {
            text.append(String.format(Locale.US,
                    "Published Table III  shift 0.503000 keV, width 2.317000 keV%n"
                    + "Difference           shift %+.6f keV, width %+.6f keV%n",
                    1_000.0 * result.strongInteractionShiftMeV() - 0.503,
                    1_000.0 * result.widthMeV() - 2.317));
        }
        text.append(String.format(Locale.US, "Elapsed               %.3f s%n%n", elapsedSeconds));
        text.append("OUTER CYCLES\n");
        text.append("cycle             input E_B                 output E_B       |dE|\n");
        for (var cycle : result.outerHistory()) {
            text.append(String.format(Locale.US,
                    "%5d % .8e%+.8ei % .8e%+.8ei %10.3e%n",
                    cycle.cycle(), cycle.inputBindingEnergyMeV().getReal(),
                    cycle.inputBindingEnergyMeV().getImaginary(),
                    cycle.outputBindingEnergyMeV().getReal(),
                    cycle.outputBindingEnergyMeV().getImaginary(), cycle.energyChangeMeV()));
        }
        JTextArea area = new JTextArea(text.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
    }

    private static boolean isTableIIISulfur(BopitProblem problem) {
        return problem.atomicSystem().nuclearCharge() == 16
                && problem.atomicSystem().massNumber() == 32
                && problem.quantumState().principalN() == 3
                && problem.quantumState().orbitalL() == 2
                && problem.waveEquation() instanceof KleinGordonSpec kg
                && kg.form() == KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR
                && problem.electromagnetic().nuclearCharge() instanceof FermiChargeSpec charge
                && close(charge.halfDensityRadiusFm(), 3.20) && close(charge.diffusenessFm(), 0.59)
                && problem.strongInteraction() instanceof KwonTabakinOpticalPotentialSpec optical
                && close(optical.fittedScatteringLengthFm().getReal(), 0.44)
                && close(optical.fittedScatteringLengthFm().getImaginary(), 0.83);
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) <= 1e-12;
    }
}
