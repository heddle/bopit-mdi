package edu.cnu.bopit.ui.editor;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.model.BopitProblem;

/** Editor for the attractive atomic system and nonrelativistic quantum state. */
public final class AtomStateEditorPanel extends JPanel {
    private final JSpinner nuclearCharge = integerSpinner(16, 1, 200, 1);
    private final JSpinner massNumber = integerSpinner(32, 1, 400, 1);
    private final JSpinner particleMass = doubleSpinner(493.667, 0.001);
    private final JSpinner nuclearMass = doubleSpinner(30032.0, 1.0);
    private final JSpinner principalN = integerSpinner(3, 1, 20, 1);
    private final JSpinner orbitalL = integerSpinner(2, 0, 19, 1);

    public AtomStateEditorPanel(Runnable onChange) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel form = EditorLayout.panel();
        EditorLayout.addRow(form, 0, "Orbiting particle", new JLabel("K- (kaon)"), "");
        EditorLayout.addRow(form, 1, "Nuclear charge, Z", nuclearCharge, "");
        EditorLayout.addRow(form, 2, "Mass number, A", massNumber, "");
        EditorLayout.addRow(form, 3, "Orbiting particle mass", particleMass, "MeV");
        EditorLayout.addRow(form, 4, "Nuclear mass", nuclearMass, "MeV");
        EditorLayout.addRow(form, 5, "Principal quantum number, n", principalN, "");
        EditorLayout.addRow(form, 6, "Orbital angular momentum, l", orbitalL, "");
        add(form, BorderLayout.NORTH);
        bind(onChange, nuclearCharge, massNumber, particleMass, nuclearMass, principalN, orbitalL);
    }

    public Values values() {
        return new Values(integer(nuclearCharge), integer(massNumber), decimal(particleMass),
                decimal(nuclearMass), integer(principalN), integer(orbitalL));
    }

    public void load(BopitProblem problem) {
        nuclearCharge.setValue(problem.atomicSystem().nuclearCharge());
        massNumber.setValue(problem.atomicSystem().massNumber());
        particleMass.setValue(problem.atomicSystem().particleMassMeV());
        nuclearMass.setValue(problem.atomicSystem().nuclearMassMeV());
        principalN.setValue(problem.quantumState().principalN());
        orbitalL.setValue(problem.quantumState().orbitalL());
    }

    public record Values(int nuclearCharge, int massNumber, double particleMassMeV,
            double nuclearMassMeV, int principalN, int orbitalL) { }

    static JSpinner integerSpinner(int value, int minimum, int maximum, int step) {
        return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
    }

    static JSpinner doubleSpinner(double value, double step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0.0, Double.MAX_VALUE, step));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.############E0"));
        return spinner;
    }

    static int integer(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    static double decimal(JSpinner spinner) {
        return ((Number) spinner.getValue()).doubleValue();
    }

    static void bind(Runnable onChange, JSpinner... spinners) {
        for (JSpinner spinner : spinners) spinner.addChangeListener(event -> onChange.run());
    }
}
