package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.physics.wavefunction.CoordinateWavefunction;
import edu.cnu.bopit.physics.wavefunction.MomentumWavefunction;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.plot.MultiplotPanel;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters.AxisScale;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Momentum- and coordinate-space views of a completed real bound state. */
public final class WavefunctionView extends BaseView {
    public WavefunctionView(PointCoulombResult result) {
        super(PropertyUtils.TITLE, "Wavefunction Diagnostics",
                PropertyUtils.WIDTH, 850, PropertyUtils.HEIGHT, 650,
                PropertyUtils.USECONTAINER, false);
        MultiplotPanel plots = new MultiplotPanel(true);
        plots.addPlot("Momentum space", momentumPlot(result));
        plots.addPlot("Coordinate space", coordinatePlot(result));
        getContentPane().add(diagnostics(result), BorderLayout.NORTH);
        getContentPane().add(plots, BorderLayout.CENTER);
    }

    private static JPanel diagnostics(PointCoulombResult result) {
        var diagnostics = result.wavefunctions().diagnostics();
        JPanel panel = new JPanel();
        panel.add(new JLabel(String.format(Locale.US,
                "Momentum norm %.9f | Coordinate norm %.9f | "
                        + "analytic overlaps: p %.9f, r %.9f | coordinate L2 error %.3e",
                diagnostics.momentumNorm(), diagnostics.coordinateNorm(),
                diagnostics.absoluteAnalyticMomentumOverlap(),
                diagnostics.absoluteAnalyticCoordinateOverlap(),
                diagnostics.signAlignedCoordinateRelativeL2Error())));
        return panel;
    }

    private static PlotPanel momentumPlot(PointCoulombResult result) {
        MomentumWavefunction numerical = result.wavefunctions().momentum();
        MomentumWavefunction analytic = result.wavefunctions().analyticMomentum();
        double[] p = numerical.momentaFmInverse();
        double[] real = numerical.radialValuesFmThreeHalves();
        double[] magnitude = magnitude(real);
        double sign = overlapSign(numerical, analytic);
        double[] exact = scaled(analytic.radialValuesFmThreeHalves(), sign);
        double[] imaginary = new double[p.length];
        var data = PlotSupport.xyData("Numerical real", "Numerical magnitude",
                "Exact Coulomb", "Imaginary (zero)");
        PlotSupport.curve(data, "Numerical real", p, real, new Color(25, 75, 160),
                SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Numerical magnitude", p, magnitude, new Color(30, 135, 70),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Exact Coulomb", p, exact, new Color(190, 75, 30),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Imaginary (zero)", p, imaginary, Color.GRAY,
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data, "Normalized momentum-space radial wavefunction",
                "Momentum p (fm^-1)", "phi_l(p) (fm^(3/2))");
        canvas.getParameters().setXScale(AxisScale.LOG10).setLegendDrawing(true);
        return new PlotPanel(canvas);
    }

    private static PlotPanel coordinatePlot(PointCoulombResult result) {
        CoordinateWavefunction numerical = result.wavefunctions().coordinate();
        CoordinateWavefunction analytic = result.wavefunctions().analyticCoordinate();
        double[] r = numerical.radiiFm();
        double[] real = numerical.radialValuesFmMinusThreeHalves();
        double sign = overlapSign(numerical, analytic);
        double[] exact = scaled(analytic.radialValuesFmMinusThreeHalves(), sign);
        double[] imaginary = new double[r.length];
        var data = PlotSupport.xyData("Transformed real", "Transformed magnitude",
                "Exact Coulomb", "Imaginary (zero)");
        PlotSupport.curve(data, "Transformed real", r, real, new Color(25, 75, 160),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Transformed magnitude", r, magnitude(real),
                new Color(30, 135, 70), SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Exact Coulomb", r, exact, new Color(190, 75, 30),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Imaginary (zero)", r, imaginary, Color.GRAY,
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data, "Fourier-Bessel coordinate-space radial wavefunction",
                "Radius r (fm)", "R_l(r) (fm^(-3/2))");
        canvas.getParameters().setLegendDrawing(true).includeXZero(true).includeYZero(true);
        return new PlotPanel(canvas);
    }

    private static double overlapSign(MomentumWavefunction first, MomentumWavefunction second) {
        double[] p = first.momentaFmInverse();
        double[] w = first.weightsFmInverse();
        double[] a = first.radialValuesFmThreeHalves();
        double[] b = second.radialValuesFmThreeHalves();
        double overlap = 0.0;
        for (int i = 0; i < p.length; i++) overlap += w[i] * p[i] * p[i] * a[i] * b[i];
        return overlap < 0.0 ? -1.0 : 1.0;
    }

    private static double overlapSign(CoordinateWavefunction first, CoordinateWavefunction second) {
        double[] r = first.radiiFm();
        double[] a = first.radialValuesFmMinusThreeHalves();
        double[] b = second.radialValuesFmMinusThreeHalves();
        double overlap = 0.0;
        for (int i = 1; i < r.length; i++) {
            double f0 = r[i - 1] * r[i - 1] * a[i - 1] * b[i - 1];
            double f1 = r[i] * r[i] * a[i] * b[i];
            overlap += 0.5 * (r[i] - r[i - 1]) * (f0 + f1);
        }
        return overlap < 0.0 ? -1.0 : 1.0;
    }

    private static double[] magnitude(double[] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) result[i] = Math.abs(values[i]);
        return result;
    }

    private static double[] scaled(double[] values, double scale) {
        double[] result = values.clone();
        for (int i = 0; i < result.length; i++) result[i] *= scale;
        return result;
    }
}
