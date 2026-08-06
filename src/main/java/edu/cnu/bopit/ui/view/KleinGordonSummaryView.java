package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.KleinGordonResult;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.mdi.view.ViewPropertiesBuilder;
import edu.cnu.mdi.view.BaseView;

/** Retained Klein-Gordon energy and nested convergence summary. */
public final class KleinGordonSummaryView extends BaseView {
    public KleinGordonSummaryView(BopitProblem problem, KleinGordonResult result,
            double elapsedSeconds) {
        super(new ViewPropertiesBuilder().title("Klein-Gordon Result")
                .width(700).height(540).useContainer(false).buildOptions());
        StringBuilder text = new StringBuilder();
        text.append("KLEIN-GORDON RESULT\n\n");
        text.append(String.format(Locale.US, "Binding energy       %.12f MeV%n", result.bindingEnergyMeV()));
        text.append(String.format(Locale.US, "Exact point Coulomb  %.12f MeV%n", result.exactPointCoulombEnergyMeV()));
        text.append(String.format(Locale.US, "Absolute difference  %.6e MeV%n",
                Math.abs(result.bindingEnergyMeV() - result.exactPointCoulombEnergyMeV())));
        text.append("Form                 ").append(problem.waveEquation()).append('\n');
        text.append("Converged            ").append(result.converged()).append('\n');
        text.append("Termination          ").append(result.terminationReason()).append('\n');
        text.append(String.format(Locale.US, "Elapsed              %.3f s%n%n", elapsedSeconds));
        text.append("OUTER CYCLES\n");
        text.append("cycle       input E_B        output E_B          |dE|       inner residual\n");
        for (var record : result.outerHistory()) {
            text.append(String.format(Locale.US, "%5d %16.9e %16.9e %12.3e %16.3e%n",
                    record.cycle(), record.inputBindingEnergyMeV(), record.outputBindingEnergyMeV(),
                    record.energyChangeMeV(), record.innerResidualMeV2()));
        }
        JTextArea area = new JTextArea(text.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
