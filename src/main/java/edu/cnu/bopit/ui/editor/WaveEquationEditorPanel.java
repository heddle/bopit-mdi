package edu.cnu.bopit.ui.editor;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.model.WaveEquationSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.DiracSpec;

/** Stage 8 wave-equation selector, initially exposing the two completed Schrödinger forms. */
public final class WaveEquationEditorPanel extends JPanel {
    private enum Kind {
        SCHRODINGER("Nonrelativistic Schrödinger"),
        RELATIVISTIC_SCHRODINGER("Relativistic-kinetic Schrödinger"),
        KG_ENERGY_WEIGHTED("Klein-Gordon: energy-weighted nuclear"),
        KG_FULL_VECTOR("Klein-Gordon: full vector"),
        KG_MASS_WEIGHTED("Klein-Gordon: mass-weighted nuclear"),
        KG_SCALAR("Klein-Gordon: scalar nuclear"),
        DIRAC("Dirac");

        private final String label;
        Kind(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final JComboBox<Kind> kind = new JComboBox<>(Kind.values());
    private final JSpinner kappa = new JSpinner(new SpinnerNumberModel(-3, -20, 20, 1));

    public WaveEquationEditorPanel(Runnable changed) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(new JLabel("Wave equation"));
        add(kind);
        add(new JLabel("kappa"));
        add(kappa);
        kind.addActionListener(event -> { updateEnabled(); changed.run(); });
        kappa.addChangeListener(event -> {
            if (((Number) kappa.getValue()).intValue() == 0) kappa.setValue(1);
            else changed.run();
        });
        updateEnabled();
    }

    public WaveEquationSpec value() {
        return switch ((Kind) kind.getSelectedItem()) {
            case RELATIVISTIC_SCHRODINGER -> new RelativisticSchrodingerSpec();
            case KG_ENERGY_WEIGHTED -> kg(KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR);
            case KG_FULL_VECTOR -> kg(KleinGordonForm.FULL_VECTOR);
            case KG_MASS_WEIGHTED -> kg(KleinGordonForm.MASS_WEIGHTED_NUCLEAR);
            case KG_SCALAR -> kg(KleinGordonForm.SCALAR_NUCLEAR);
            case DIRAC -> new DiracSpec(((Number) kappa.getValue()).intValue());
            default -> new SchrodingerSpec();
        };
    }

    public void load(WaveEquationSpec equation) {
        kind.setSelectedItem(equation instanceof RelativisticSchrodingerSpec
                ? Kind.RELATIVISTIC_SCHRODINGER
                : equation instanceof KleinGordonSpec kg ? switch (kg.form()) {
                    case ENERGY_WEIGHTED_NUCLEAR -> Kind.KG_ENERGY_WEIGHTED;
                    case FULL_VECTOR -> Kind.KG_FULL_VECTOR;
                    case MASS_WEIGHTED_NUCLEAR -> Kind.KG_MASS_WEIGHTED;
                    case SCALAR_NUCLEAR -> Kind.KG_SCALAR;
                } : equation instanceof DiracSpec ? Kind.DIRAC
                : Kind.SCHRODINGER);
        if (equation instanceof DiracSpec dirac) kappa.setValue(dirac.kappa());
        updateEnabled();
    }

    private static KleinGordonSpec kg(KleinGordonForm form) {
        return new KleinGordonSpec(form, 1e-9, 15);
    }

    private void updateEnabled() {
        kappa.setEnabled(kind.getSelectedItem() == Kind.DIRAC);
    }
}
