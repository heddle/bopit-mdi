package edu.cnu.bopit.ui.editor;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.model.BopitProblem;

/** Editor for shifted inverse-iteration convergence controls. */
public final class SolverEditorPanel extends JPanel {
    private final JSpinner shift = signedDoubleSpinner(-0.37, 0.01);
    private final JSpinner energyTolerance = AtomStateEditorPanel.doubleSpinner(1e-12, 1e-12);
    private final JSpinner residualTolerance = AtomStateEditorPanel.doubleSpinner(1e-10, 1e-10);
    private final JSpinner minimumIterations = AtomStateEditorPanel.integerSpinner(5, 1, 10000, 1);
    private final JSpinner maximumIterations = AtomStateEditorPanel.integerSpinner(50, 1, 10000, 1);

    public SolverEditorPanel(Runnable onChange) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel form = EditorLayout.panel();
        EditorLayout.addRow(form, 0, "Inverse-iteration shift", shift, "MeV");
        EditorLayout.addRow(form, 1, "Energy-change tolerance", energyTolerance, "MeV");
        EditorLayout.addRow(form, 2, "Residual tolerance", residualTolerance, "dimensionless");
        EditorLayout.addRow(form, 3, "Minimum iterations", minimumIterations, "iterations");
        EditorLayout.addRow(form, 4, "Maximum iterations", maximumIterations, "iterations");
        add(form, BorderLayout.NORTH);
        AtomStateEditorPanel.bind(onChange, shift, energyTolerance, residualTolerance,
                minimumIterations, maximumIterations);
    }

    public Values values() {
        return new Values(AtomStateEditorPanel.decimal(shift),
                AtomStateEditorPanel.decimal(energyTolerance),
                AtomStateEditorPanel.decimal(residualTolerance),
                AtomStateEditorPanel.integer(minimumIterations),
                AtomStateEditorPanel.integer(maximumIterations));
    }

    public void load(BopitProblem problem) {
        shift.setValue(problem.solver().shiftMeV());
        energyTolerance.setValue(problem.solver().energyTolerance());
        residualTolerance.setValue(problem.solver().residualTolerance());
        minimumIterations.setValue(problem.solver().minimumIterations());
        maximumIterations.setValue(problem.solver().maximumIterations());
    }

    private static JSpinner signedDoubleSpinner(double value, double step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value,
                -Double.MAX_VALUE, Double.MAX_VALUE, step));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.############E0"));
        return spinner;
    }

    public record Values(double shiftMeV, double energyToleranceMeV,
            double residualTolerance, int minimumIterations, int maximumIterations) { }
}
