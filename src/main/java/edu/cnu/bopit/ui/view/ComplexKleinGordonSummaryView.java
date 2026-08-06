package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.ComplexKleinGordonResult;
import edu.cnu.bopit.model.BopitProblem;
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
}
