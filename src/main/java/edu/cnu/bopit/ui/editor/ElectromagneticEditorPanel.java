package edu.cnu.bopit.ui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.GaussianChargeSpec;
import edu.cnu.bopit.model.PointChargeSpec;
import edu.cnu.bopit.model.UniformChargeSpec;

/** Editor for Stage 6 nuclear charge and first-order vacuum polarization. */
public final class ElectromagneticEditorPanel extends JPanel {
    private enum Kind { POINT, UNIFORM, GAUSSIAN, FERMI }

    private final JComboBox<Kind> kind = new JComboBox<>(Kind.values());
    private final JTextField radius = new JTextField("2.56", 10);
    private final JTextField diffuseness = new JTextField("0.59", 10);
    private final JTextField fermiW = new JTextField("0.0", 10);
    private final JCheckBox uehling = new JCheckBox("Include first-order Uehling correction");

    public ElectromagneticEditorPanel(Runnable changed) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets.set(4, 4, 4, 8);
        addRow("Nuclear charge model", kind, constraints, 0);
        addRow("Rms radius or Fermi c", radius, constraints, 1);
        addRow("Radius unit", new JLabel("fm"), constraints, 2);
        addRow("Fermi diffuseness a", diffuseness, constraints, 3);
        addRow("Diffuseness unit", new JLabel("fm"), constraints, 4);
        addRow("Fermi w", fermiW, constraints, 5);
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        add(uehling, constraints);
        kind.addActionListener(event -> { updateEnabled(); changed.run(); });
        uehling.addActionListener(event -> changed.run());
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { changed.run(); }
            @Override public void removeUpdate(DocumentEvent event) { changed.run(); }
            @Override public void changedUpdate(DocumentEvent event) { changed.run(); }
        };
        radius.getDocument().addDocumentListener(listener);
        diffuseness.getDocument().addDocumentListener(listener);
        fermiW.getDocument().addDocumentListener(listener);
        updateEnabled();
    }

    public void loadPointCharge() {
        kind.setSelectedItem(Kind.POINT);
        uehling.setSelected(false);
        updateEnabled();
    }

    /** Load a validated immutable electromagnetic specification. */
    public void load(ElectromagneticSpec spec) {
        var charge = spec.nuclearCharge();
        if (charge instanceof PointChargeSpec) kind.setSelectedItem(Kind.POINT);
        else if (charge instanceof UniformChargeSpec uniform) {
            kind.setSelectedItem(Kind.UNIFORM); radius.setText(Double.toString(uniform.rmsRadiusFm()));
        } else if (charge instanceof GaussianChargeSpec gaussian) {
            kind.setSelectedItem(Kind.GAUSSIAN); radius.setText(Double.toString(gaussian.rmsRadiusFm()));
        } else if (charge instanceof FermiChargeSpec fermi) {
            kind.setSelectedItem(Kind.FERMI);
            radius.setText(Double.toString(fermi.halfDensityRadiusFm()));
            diffuseness.setText(Double.toString(fermi.diffusenessFm()));
            fermiW.setText(Double.toString(fermi.w()));
        }
        uehling.setSelected(spec.uehlingVacuumPolarization());
        updateEnabled();
    }

    public ElectromagneticSpec values() {
        Kind selected = (Kind) kind.getSelectedItem();
        var charge = switch (selected) {
            case POINT -> new PointChargeSpec();
            case UNIFORM -> new UniformChargeSpec(parse(radius));
            case GAUSSIAN -> new GaussianChargeSpec(parse(radius));
            case FERMI -> new FermiChargeSpec(parse(radius), parse(diffuseness), parse(fermiW));
        };
        return new ElectromagneticSpec(charge, uehling.isSelected());
    }

    public List<String> validationErrors() {
        List<String> errors = new ArrayList<>();
        try { values(); } catch (IllegalArgumentException error) {
            errors.add("Electromagnetic inputs: " + error.getMessage());
        }
        return errors;
    }

    private void updateEnabled() {
        Kind selected = (Kind) kind.getSelectedItem();
        radius.setEnabled(selected != Kind.POINT);
        diffuseness.setEnabled(selected == Kind.FERMI);
        fermiW.setEnabled(selected == Kind.FERMI);
    }

    private static double parse(JTextField field) {
        try { return Double.parseDouble(field.getText().trim()); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("values must be numbers"); }
    }

    private void addRow(String label, java.awt.Component component,
            GridBagConstraints constraints, int row) {
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = row;
        add(new JLabel(label), constraints);
        constraints.gridx = 1;
        add(component, constraints);
    }
}
