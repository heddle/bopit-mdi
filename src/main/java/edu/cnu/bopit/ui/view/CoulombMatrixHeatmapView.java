package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.mdi.splot.pdata.Histo2DData;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Linear/logarithmic magnitude heatmap of the symmetric-basis Coulomb operator. */
public final class CoulombMatrixHeatmapView extends BaseView {
    public CoulombMatrixHeatmapView(PointCoulombResult result) {
        super(PropertyUtils.TITLE, "Coulomb Matrix Diagnostics",
                PropertyUtils.WIDTH, 700, PropertyUtils.HEIGHT, 650,
                PropertyUtils.USECONTAINER, false);
        RealMatrix matrix = result.coulombOperatorMeV();
        int size = matrix.getRowDimension();
        Histo2DData histogram = new Histo2DData("|H_ij|", 0.0, size, size, 0.0, size, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double magnitude = Math.abs(matrix.getEntry(i, j));
                if (magnitude > 0.0) histogram.fill(i + 0.5, j + 0.5, magnitude);
            }
        }
        PlotCanvas canvas;
        try {
            canvas = new PlotCanvas(new PlotData(histogram), "Landé-regularized Coulomb matrix magnitude",
                    "Grid index i", "Grid index j");
        } catch (PlotDataException exception) {
            throw new IllegalStateException("could not create Coulomb matrix heatmap", exception);
        }
        canvas.getParameters().setLogZ(true).setShowEmptyBins(true);
        JCheckBox logMagnitude = new JCheckBox("Logarithmic magnitude", true);
        logMagnitude.addActionListener(event -> {
            canvas.getParameters().setLogZ(logMagnitude.isSelected());
            canvas.repaint();
        });
        JPanel controls = new JPanel();
        controls.add(logMagnitude);
        getContentPane().add(controls, BorderLayout.NORTH);
        getContentPane().add(new PlotPanel(canvas), BorderLayout.CENTER);
    }
}
