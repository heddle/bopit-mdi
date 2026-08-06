package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cnu.bopit.calculation.DiracResult;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Coupled-component Dirac energy and normalization summary. */
public final class DiracSummaryView extends BaseView {
    public DiracSummaryView(BopitProblem problem, DiracResult result, double elapsedSeconds) {
        super(PropertyUtils.TITLE, "Dirac Result", PropertyUtils.WIDTH, 650,
                PropertyUtils.HEIGHT, 460, PropertyUtils.USECONTAINER, false);
        String text = String.format(Locale.US,
                "DIRAC RESULT%n%n"
                + "Specification       %s%n"
                + "Binding energy      %.12f MeV%n"
                + "Exact point Coulomb %.12f MeV%n"
                + "Absolute difference %.6e MeV%n"
                + "Large norm fraction %.9f%n"
                + "Small norm fraction %.9f%n"
                + "Iterations          %d%n"
                + "Converged           %s%n"
                + "Elapsed             %.3f s%n",
                problem.waveEquation(), result.bindingEnergyMeV(),
                result.exactPointCoulombEnergyMeV(), result.absoluteErrorMeV(),
                result.largeComponentNorm(), result.smallComponentNorm(),
                result.solverResult().history().size(), result.solverResult().converged(),
                elapsedSeconds);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
