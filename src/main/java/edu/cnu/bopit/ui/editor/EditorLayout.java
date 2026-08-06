package edu.cnu.bopit.ui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class EditorLayout {
    private EditorLayout() { }

    static JPanel panel() {
        return new JPanel(new GridBagLayout());
    }

    static void addRow(JPanel panel, int row, String label, JComponent input, String units) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(input, c);
        c.gridx = 2;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(units == null ? "" : units), c);
    }
}
