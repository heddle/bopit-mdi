package edu.cnu.bopit.ui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.StrongInteractionSpec;

/** Editor for the Stage 7 local complex optical potential. */
public final class StrongInteractionEditorPanel extends JPanel {
    private enum Kind { NONE, KWON_TABAKIN_LOCAL }
    private final JComboBox<Kind> kind = new JComboBox<>(Kind.values());
    private final JTextField scatteringReal = new JTextField("-0.44", 10);
    private final JTextField scatteringImaginary = new JTextField("0.81", 10);
    private final JTextField fermiC = new JTextField("3.20", 10);
    private final JTextField fermiA = new JTextField("0.59", 10);
    private final JTextField fermiW = new JTextField("0.0", 10);

    public StrongInteractionEditorPanel(Runnable changed) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addRow(0, "Strong interaction", kind, "");
        addRow(1, "Re fitted scattering length", scatteringReal, "fm");
        addRow(2, "Im fitted scattering length", scatteringImaginary, "fm");
        addRow(3, "Fermi half-density radius c", fermiC, "fm");
        addRow(4, "Fermi diffuseness a", fermiA, "fm");
        addRow(5, "Fermi w", fermiW, "dimensionless");
        kind.addActionListener(event -> { updateEnabled(); changed.run(); });
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { changed.run(); }
            @Override public void removeUpdate(DocumentEvent event) { changed.run(); }
            @Override public void changedUpdate(DocumentEvent event) { changed.run(); }
        };
        for (JTextField field : List.of(scatteringReal, scatteringImaginary, fermiC, fermiA, fermiW)) {
            field.getDocument().addDocumentListener(listener);
        }
        updateEnabled();
    }

    public void loadNone() { kind.setSelectedItem(Kind.NONE); updateEnabled(); }

    public StrongInteractionSpec values() {
        if (kind.getSelectedItem() == Kind.NONE) return new NoStrongInteractionSpec();
        return new KwonTabakinOpticalPotentialSpec(
                new Complex(parse(scatteringReal), parse(scatteringImaginary)),
                new FermiChargeSpec(parse(fermiC), parse(fermiA), parse(fermiW)));
    }

    public List<String> validationErrors() {
        try { values(); return List.of(); }
        catch (IllegalArgumentException error) { return List.of("Strong interaction: " + error.getMessage()); }
    }

    private void updateEnabled() {
        boolean enabled = kind.getSelectedItem() != Kind.NONE;
        for (JTextField field : List.of(scatteringReal, scatteringImaginary, fermiC, fermiA, fermiW)) {
            field.setEnabled(enabled);
        }
    }

    private void addRow(int row, String label, java.awt.Component value, String units) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets.set(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        add(new JLabel(label), c);
        c.gridx = 1;
        add(value, c);
        c.gridx = 2;
        add(new JLabel(units), c);
    }

    private static double parse(JTextField field) {
        try { return Double.parseDouble(field.getText().trim()); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("values must be numbers"); }
    }
}
