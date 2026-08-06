package edu.cnu.bopit.ui.editor;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.OrbitingParticle;

/** Editor for the attractive atomic system and nonrelativistic quantum state. */
public final class AtomStateEditorPanel extends JPanel {
    private final JSpinner nuclearCharge = integerSpinner(16, 1, 200, 1);
    private final JSpinner massNumber = integerSpinner(32, 1, 400, 1);
    private final JComboBox<OrbitingParticle> particle = new JComboBox<>(OrbitingParticle.values());
    private final JLabel particleMass = new JLabel();
    private double selectedParticleMassMeV;
    private final JSpinner nuclearMass = doubleSpinner(30032.0, 1.0);
    private final JSpinner principalN = integerSpinner(3, 1, 20, 1);
    private final JSpinner orbitalL = integerSpinner(2, 0, 19, 1);

    public AtomStateEditorPanel(Runnable onChange) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel form = EditorLayout.panel();
        particle.setSelectedItem(OrbitingParticle.KAON_MINUS);
        EditorLayout.addRow(form, 0, "Orbiting particle", particle, "");
        EditorLayout.addRow(form, 1, "Nuclear charge, Z", nuclearCharge, "");
        EditorLayout.addRow(form, 2, "Mass number, A", massNumber, "");
        EditorLayout.addRow(form, 3, "Orbiting particle mass", particleMass, "MeV");
        EditorLayout.addRow(form, 4, "Nuclear mass", nuclearMass, "MeV");
        EditorLayout.addRow(form, 5, "Principal quantum number, n", principalN, "");
        EditorLayout.addRow(form, 6, "Orbital angular momentum, l", orbitalL, "");
        add(form, BorderLayout.NORTH);
        particle.addActionListener(event -> { updateParticleMass(); onChange.run(); });
        bind(onChange, nuclearCharge, massNumber, nuclearMass, principalN, orbitalL);
        updateParticleMass();
    }

    public Values values() {
        OrbitingParticle selected = (OrbitingParticle) particle.getSelectedItem();
        return new Values(integer(nuclearCharge), integer(massNumber), selected,
                selectedParticleMassMeV,
                decimal(nuclearMass), integer(principalN), integer(orbitalL));
    }

    public void load(BopitProblem problem) {
        nuclearCharge.setValue(problem.atomicSystem().nuclearCharge());
        massNumber.setValue(problem.atomicSystem().massNumber());
        particle.setSelectedItem(problem.atomicSystem().particle());
        selectedParticleMassMeV = problem.atomicSystem().particleMassMeV();
        updateParticleMassLabel();
        nuclearMass.setValue(problem.atomicSystem().nuclearMassMeV());
        principalN.setValue(problem.quantumState().principalN());
        orbitalL.setValue(problem.quantumState().orbitalL());
    }

    private void updateParticleMass() {
        OrbitingParticle selected = (OrbitingParticle) particle.getSelectedItem();
        selectedParticleMassMeV = selected == null ? Double.NaN : selected.recommendedMassMeV();
        updateParticleMassLabel();
    }

    private void updateParticleMassLabel() {
        particleMass.setText(Double.isFinite(selectedParticleMassMeV)
                ? Double.toString(selectedParticleMassMeV) : "");
    }

    /** Replace a loaded historical mass with the selected particle's PDG-2024 value. */
    public void useRecommendedParticleMass() { updateParticleMass(); }

    public record Values(int nuclearCharge, int massNumber, OrbitingParticle particle,
            double particleMassMeV,
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
