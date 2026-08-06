package edu.cnu.bopit.ui.editor;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.ui.workbench.GridKind;

/** Editor for legacy and adaptive momentum-grid specifications. */
public final class GridEditorPanel extends JPanel {
    private final JComboBox<GridKind> gridKind = new JComboBox<>(GridKind.values());
    private final JSpinner totalPoints = AtomStateEditorPanel.integerSpinner(40, 4, 1000, 2);
    private final JSpinner nuclearPoints = AtomStateEditorPanel.integerSpinner(10, 1, 998, 1);
    private final JSpinner atomicScale = AtomStateEditorPanel.doubleSpinner(0.095, 0.005);
    private final JSpinner nuclearScale = AtomStateEditorPanel.doubleSpinner(0.7, 0.05);
    private final JSpinner boundary = AtomStateEditorPanel.doubleSpinner(0.3, 0.05);
    private final JSpinner maximumScale = AtomStateEditorPanel.doubleSpinner(1000.0, 100.0);
    private final JSpinner maximumMomentum = AtomStateEditorPanel.doubleSpinner(100000.0, 1000.0);
    private final JSpinner regionBias = new JSpinner(new javax.swing.SpinnerNumberModel(0.5, 0.001, 0.999, 0.05));
    private final JPanel methodCards = new JPanel(new CardLayout());

    public GridEditorPanel(Runnable onChange) {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel common = EditorLayout.panel();
        EditorLayout.addRow(common, 0, "Grid method", gridKind, "");
        EditorLayout.addRow(common, 1, "Total quadrature points", totalPoints, "points");
        EditorLayout.addRow(common, 2, "Nuclear-region points", nuclearPoints, "points");
        add(common, BorderLayout.NORTH);

        JPanel legacy = EditorLayout.panel();
        EditorLayout.addRow(legacy, 0, "Atomic scale (legacy CATOM)", atomicScale, "fm^-1");
        EditorLayout.addRow(legacy, 1, "Nuclear scale (legacy CNUCL)", nuclearScale, "fm^-1");
        EditorLayout.addRow(legacy, 2, "Region boundary (legacy CSIZE)", boundary, "fm^-1");
        EditorLayout.addRow(legacy, 3, "Maximum scale (legacy CPMAX)", maximumScale, "dimensionless");
        JPanel adaptive = EditorLayout.panel();
        EditorLayout.addRow(adaptive, 0, "Maximum momentum", maximumMomentum, "fm^-1");
        EditorLayout.addRow(adaptive, 1, "Region bias", regionBias, "dimensionless");
        methodCards.add(legacy, GridKind.LEGACY.name());
        methodCards.add(adaptive, GridKind.ADAPTIVE.name());
        add(methodCards, BorderLayout.CENTER);

        gridKind.addActionListener(event -> {
            showSelectedCard();
            onChange.run();
        });
        AtomStateEditorPanel.bind(onChange, totalPoints, nuclearPoints, atomicScale,
                nuclearScale, boundary, maximumScale, maximumMomentum, regionBias);
        showSelectedCard();
    }

    public Values values() {
        return new Values((GridKind) gridKind.getSelectedItem(),
                AtomStateEditorPanel.integer(totalPoints), AtomStateEditorPanel.integer(nuclearPoints),
                AtomStateEditorPanel.decimal(atomicScale), AtomStateEditorPanel.decimal(nuclearScale),
                AtomStateEditorPanel.decimal(boundary), AtomStateEditorPanel.decimal(maximumScale),
                AtomStateEditorPanel.decimal(maximumMomentum), AtomStateEditorPanel.decimal(regionBias));
    }

    public void load(BopitProblem problem) {
        totalPoints.setValue(problem.grid().totalPoints());
        if (problem.grid() instanceof LegacyGridSpec legacy) {
            gridKind.setSelectedItem(GridKind.LEGACY);
            nuclearPoints.setValue(legacy.nuclearPoints());
            atomicScale.setValue(legacy.atomicScale());
            nuclearScale.setValue(legacy.nuclearScale());
            boundary.setValue(legacy.regionBoundary());
            maximumScale.setValue(legacy.maximumMomentumScale());
        } else if (problem.grid() instanceof AdaptiveGridSpec adaptive) {
            gridKind.setSelectedItem(GridKind.ADAPTIVE);
            nuclearPoints.setValue(adaptive.nuclearPoints());
            maximumMomentum.setValue(adaptive.maximumMomentum());
            regionBias.setValue(adaptive.regionBias());
        }
        showSelectedCard();
    }

    private void showSelectedCard() {
        ((CardLayout) methodCards.getLayout()).show(methodCards,
                ((GridKind) gridKind.getSelectedItem()).name());
    }

    public record Values(GridKind gridKind, int totalPoints, int nuclearPoints,
            double legacyAtomicScaleFmInverse, double legacyNuclearScaleFmInverse,
            double legacyBoundaryFmInverse, double legacyMaximumMomentumScale,
            double adaptiveMaximumMomentumFmInverse, double adaptiveRegionBias) { }
}
