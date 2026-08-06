package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.diagnostic.LandeIntegrandProfile;
import edu.cnu.bopit.calculation.diagnostic.LandeIntegrandProfileFactory;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.Curve;
import edu.cnu.mdi.splot.plot.MultiplotPanel;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters.AxisScale;
import edu.cnu.mdi.splot.plot.VerticalLine;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Interactive inspection of the point-Coulomb Landé subtraction at one grid point. */
public final class LandeDiagnosticView extends BaseView {
    private final BopitProblem problem;
    private final PointCoulombResult result;
    private final PhysicalConstantSet constants;
    private final JLabel values = new JLabel();
    private final Curve kernelCurve;
    private final Curve ordinaryCurve;
    private final Curve subtractionCurve;
    private final Curve regularizedCurve;
    private final PlotCanvas kernelCanvas;
    private final PlotCanvas integrandCanvas;
    private final PlotCanvas regularizedCanvas;

    public LandeDiagnosticView(BopitProblem problem, PointCoulombResult result,
            PhysicalConstantSet constants) {
        super(PropertyUtils.TITLE, "Landé Subtraction Diagnostics",
                PropertyUtils.WIDTH, 850, PropertyUtils.HEIGHT, 650,
                PropertyUtils.USECONTAINER, false);
        this.problem = problem;
        this.result = result;
        this.constants = constants;
        int initialIndex = result.grid().size() / 2;
        LandeIntegrandProfile initial = profile(initialIndex);

        var kernelData = PlotSupport.xyData("Singular Coulomb kernel");
        kernelCurve = PlotSupport.curve(kernelData, "Singular Coulomb kernel",
                initial.otherMomentaFmInverse(), initial.singularKernelMeVFm3(),
                new Color(150, 45, 35), SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        kernelCanvas = canvas(kernelData, "Point-Coulomb kernel away from p'=p",
                "V_l(p,p') (MeV fm^3)");

        var integrandData = PlotSupport.xyData("Ordinary integrand", "Subtraction integrand");
        ordinaryCurve = PlotSupport.curve(integrandData, "Ordinary integrand",
                initial.otherMomentaFmInverse(), initial.ordinaryIntegrand(),
                new Color(25, 75, 160), SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        subtractionCurve = PlotSupport.curve(integrandData, "Subtraction integrand",
                initial.otherMomentaFmInverse(), initial.subtractionIntegrand(),
                new Color(190, 100, 20), SymbolType.DIAMOND, CurveDrawingMethod.CONNECT);
        integrandCanvas = canvas(integrandData, "Landé ordinary and subtraction terms",
                "Integrand (arbitrary eigenvector scale)");

        var regularizedData = PlotSupport.xyData("Regularized integrand");
        regularizedCurve = PlotSupport.curve(regularizedData, "Regularized integrand",
                initial.otherMomentaFmInverse(), initial.regularizedIntegrand(),
                new Color(25, 125, 65), SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        regularizedCanvas = canvas(regularizedData, "Finite Landé-regularized integrand",
                "Regularized integrand (arbitrary scale)");

        MultiplotPanel gallery = new MultiplotPanel(true);
        gallery.addPlot("Singular kernel", new PlotPanel(kernelCanvas));
        gallery.addPlot("Subtraction terms", new PlotPanel(integrandCanvas));
        gallery.addPlot("Regularized integrand", new PlotPanel(regularizedCanvas));
        getContentPane().add(createControls(initialIndex), BorderLayout.NORTH);
        getContentPane().add(gallery, BorderLayout.CENTER);
        update(initialIndex);
    }

    private JPanel createControls(int initialIndex) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Selected grid index:"));
        JSpinner index = new JSpinner(new SpinnerNumberModel(initialIndex, 0,
                result.grid().size() - 1, 1));
        index.addChangeListener(event -> update(((Number) index.getValue()).intValue()));
        panel.add(index);
        panel.add(values);
        return panel;
    }

    private PlotCanvas canvas(edu.cnu.mdi.splot.pdata.PlotData data, String title, String yLabel) {
        PlotCanvas canvas = new PlotCanvas(data, title, "Other momentum p' (fm^-1)", yLabel);
        canvas.getParameters().setXScale(AxisScale.LOG10).setLegendDrawing(true);
        return canvas;
    }

    private void update(int selectedIndex) {
        LandeIntegrandProfile profile = profile(selectedIndex);
        replace(kernelCurve, profile.otherMomentaFmInverse(), profile.singularKernelMeVFm3());
        replace(ordinaryCurve, profile.otherMomentaFmInverse(), profile.ordinaryIntegrand());
        replace(subtractionCurve, profile.otherMomentaFmInverse(), profile.subtractionIntegrand());
        replace(regularizedCurve, profile.otherMomentaFmInverse(), profile.regularizedIntegrand());
        markSelection(kernelCanvas, profile.selectedMomentumFmInverse());
        markSelection(integrandCanvas, profile.selectedMomentumFmInverse());
        markSelection(regularizedCanvas, profile.selectedMomentumFmInverse());
        values.setText(String.format(Locale.US,
                "p = %.6g fm^-1; analytic = %.6g MeV fm; discrete sum = %.6g; diagonal kernel = %.6g",
                profile.selectedMomentumFmInverse(), profile.analyticTermMeVFm(),
                profile.discreteSubtractionSum(), profile.effectiveDiagonalKernel()));
    }

    private LandeIntegrandProfile profile(int selectedIndex) {
        return LandeIntegrandProfileFactory.create(problem, result, constants, selectedIndex);
    }

    private static void replace(Curve curve, double[] x, double[] y) {
        curve.clearData();
        curve.addAll(x, y);
    }

    private static void markSelection(PlotCanvas canvas, double momentum) {
        canvas.getParameters().clearPlotLines();
        canvas.getParameters().addPlotLine(new VerticalLine(canvas, momentum));
        canvas.setWorldSystem();
        canvas.repaint();
    }
}
