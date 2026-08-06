package edu.cnu.bopit.ui.editor;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.model.WaveEquationSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;

/** Stage 8 wave-equation selector, initially exposing the two completed Schrödinger forms. */
public final class WaveEquationEditorPanel extends JPanel {
    private enum Kind {
        SCHRODINGER("Nonrelativistic Schrödinger"),
        RELATIVISTIC_SCHRODINGER("Relativistic-kinetic Schrödinger"),
        KLEIN_GORDON_VECTOR("Klein-Gordon (vector Coulomb)");

        private final String label;
        Kind(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final JComboBox<Kind> kind = new JComboBox<>(Kind.values());

    public WaveEquationEditorPanel(Runnable changed) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(new JLabel("Wave equation"));
        add(kind);
        kind.addActionListener(event -> changed.run());
    }

    public WaveEquationSpec value() {
        return switch ((Kind) kind.getSelectedItem()) {
            case RELATIVISTIC_SCHRODINGER -> new RelativisticSchrodingerSpec();
            case KLEIN_GORDON_VECTOR -> new KleinGordonSpec(
                    KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR, 1e-10, 12);
            default -> new SchrodingerSpec();
        };
    }

    public void load(WaveEquationSpec equation) {
        kind.setSelectedItem(equation instanceof RelativisticSchrodingerSpec
                ? Kind.RELATIVISTIC_SCHRODINGER
                : equation instanceof KleinGordonSpec ? Kind.KLEIN_GORDON_VECTOR
                : Kind.SCHRODINGER);
    }
}
