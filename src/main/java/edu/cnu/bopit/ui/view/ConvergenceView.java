package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.solver.IterationRecord;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.plot.HorizontalLine;
import edu.cnu.mdi.splot.plot.PlotDeck;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters.AxisScale;
import edu.cnu.mdi.view.ViewPropertiesBuilder;
import edu.cnu.mdi.view.BaseView;

/** Gallery of inverse-iteration eigenvalue, energy-change, and residual plots. */
public final class ConvergenceView extends BaseView {
    public ConvergenceView(PointCoulombResult result) {
        super(new ViewPropertiesBuilder().title("Inverse-Iteration Convergence")
                .width(780).height(590).useContainer(false).buildOptions());
        PlotDeck gallery = new PlotDeck(true);
        gallery.addPlot("Eigenvalue", eigenvaluePlot(result));
        gallery.addPlot("Energy change", positivePlot(result.solverResult().history(), true));
        gallery.addPlot("Residual", positivePlot(result.solverResult().history(), false));
        getContentPane().add(gallery, BorderLayout.CENTER);
    }

    private static PlotPanel eigenvaluePlot(PointCoulombResult result) {
        List<IterationRecord> history = result.solverResult().history();
        double[] iteration = new double[history.size()];
        double[] energy = new double[history.size()];
        for (int i = 0; i < history.size(); i++) {
            iteration[i] = history.get(i).iteration();
            energy[i] = history.get(i).eigenvalueMeV();
        }
        var data = PlotSupport.xyData("Rayleigh estimate");
        PlotSupport.curve(data, "Rayleigh estimate", iteration, energy,
                new Color(25, 70, 155), SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data, "Eigenvalue estimate by iteration",
                "Iteration", "Binding energy (MeV)");
        canvas.getParameters().setLegendDrawing(true)
                .addPlotLine(new HorizontalLine(canvas, result.referenceEnergyMeV()));
        return new PlotPanel(canvas);
    }

    private static PlotPanel positivePlot(List<IterationRecord> history, boolean energyChange) {
        int start = energyChange ? 1 : 0;
        double[] iteration = new double[history.size() - start];
        double[] value = new double[iteration.length];
        for (int i = start; i < history.size(); i++) {
            iteration[i - start] = history.get(i).iteration();
            value[i - start] = Math.max(Double.MIN_NORMAL,
                    energyChange ? history.get(i).energyChangeMeV() : history.get(i).residualNorm());
        }
        String name = energyChange ? "|Delta E|" : "Residual norm";
        var data = PlotSupport.xyData(name);
        PlotSupport.curve(data, name, iteration, value,
                energyChange ? new Color(175, 75, 25) : new Color(35, 125, 65),
                SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data,
                energyChange ? "Energy change by iteration" : "Eigenvector residual by iteration",
                "Iteration", energyChange ? "|Delta E| (MeV)" : "Relative residual");
        canvas.getParameters().setYScale(AxisScale.LOG10).setLegendDrawing(true);
        return new PlotPanel(canvas);
    }
}
