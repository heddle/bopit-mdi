package edu.cnu.bopit.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.diagnostic.CoulombMomentumProfile;
import edu.cnu.bopit.calculation.diagnostic.CoulombMomentumProfileFactory;
import edu.cnu.bopit.grid.AdaptiveGridDiagnostics;
import edu.cnu.bopit.grid.GridRegion;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.LineStyle;
import edu.cnu.mdi.graphics.style.Styled;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters.AxisScale;
import edu.cnu.mdi.splot.plot.VerticalLine;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Log-momentum view of the analytic Coulomb shape and quadrature placement. */
public final class MomentumGridView extends BaseView {
    public MomentumGridView(BopitProblem problem, PointCoulombResult result,
            PhysicalConstantSet constants) {
        super(PropertyUtils.TITLE, "Momentum Grid Diagnostics",
                PropertyUtils.WIDTH, 820, PropertyUtils.HEIGHT, 620,
                PropertyUtils.USECONTAINER, false);
        double minimum = result.grid().point(0);
        double maximum = result.grid().point(result.grid().size() - 1);
        CoulombMomentumProfile profile = CoulombMomentumProfileFactory.create(
                problem.atomicSystem(), problem.quantumState(), constants,
                minimum, maximum, 600);
        PlotData data = PlotSupport.xyData("Coulomb |phi(p)| / max", "Atomic grid points",
                "Nuclear grid points");
        PlotSupport.curve(data, "Coulomb |phi(p)| / max", profile.momentaFmInverse(),
                profile.normalizedMagnitude(), new Color(25, 70, 155), SymbolType.NOSYMBOL,
                CurveDrawingMethod.CONNECT);
        addRegionPoints(data, result, profile);

        PlotCanvas canvas = new PlotCanvas(data, "Point-Coulomb momentum grid",
                "Momentum p (fm^-1)", "Normalized magnitude (arbitrary scale)");
        canvas.getParameters().setXScale(AxisScale.LOG10).includeYZero(true)
                .setLegendDrawing(true).setMinExponentX(4).setNumDecimalX(3);
        addBoundaries(canvas, result);
        getContentPane().add(new PlotPanel(canvas), BorderLayout.CENTER);
    }

    private static void addRegionPoints(PlotData data, PointCoulombResult result,
            CoulombMomentumProfile profile) {
        List<Double> atomicX = new ArrayList<>();
        List<Double> atomicY = new ArrayList<>();
        List<Double> nuclearX = new ArrayList<>();
        List<Double> nuclearY = new ArrayList<>();
        for (GridRegion region : result.grid().regions()) {
            for (int i = region.startIndex(); i < region.endIndex(); i++) {
                double p = result.grid().point(i);
                double value = interpolate(profile, p);
                (region.type() == GridRegion.Type.ATOMIC ? atomicX : nuclearX).add(p);
                (region.type() == GridRegion.Type.ATOMIC ? atomicY : nuclearY).add(value);
            }
        }
        PlotSupport.curve(data, "Atomic grid points", array(atomicX), array(atomicY),
                new Color(25, 135, 70), SymbolType.CIRCLE, CurveDrawingMethod.NONE);
        PlotSupport.curve(data, "Nuclear grid points", array(nuclearX), array(nuclearY),
                new Color(190, 65, 35), SymbolType.DIAMOND, CurveDrawingMethod.NONE);
    }

    private static void addBoundaries(PlotCanvas canvas, PointCoulombResult result) {
        for (GridRegion region : result.grid().regions()) {
            addLine(canvas, region.lowerLimit(), new Color(115, 115, 115), LineStyle.DASH);
            addLine(canvas, region.upperLimit(), new Color(115, 115, 115), LineStyle.DASH);
        }
        if (result.grid().diagnostics() instanceof AdaptiveGridDiagnostics adaptive) {
            for (double node : adaptive.atomicNodes()) {
                addLine(canvas, node, new Color(125, 35, 150), LineStyle.DOT_DASH);
            }
            addLine(canvas, adaptive.nuclearRegionStart(), new Color(190, 65, 35), LineStyle.LONG_DASH);
        }
    }

    private static void addLine(PlotCanvas canvas, double momentum, Color color, LineStyle lineStyle) {
        if (momentum <= 0.0) return;
        VerticalLine line = new VerticalLine(canvas, momentum);
        Styled style = new Styled();
        style.setAuxLineColor(color);
        style.setAuxLineStyle(lineStyle);
        style.setAuxLineWidth(1.2f);
        line.setStyle(style);
        canvas.getParameters().addPlotLine(line);
    }

    private static double interpolate(CoulombMomentumProfile profile, double p) {
        double[] x = profile.momentaFmInverse();
        double[] y = profile.normalizedMagnitude();
        int low = 0;
        int high = x.length - 1;
        while (high - low > 1) {
            int middle = (low + high) >>> 1;
            if (x[middle] <= p) low = middle; else high = middle;
        }
        double fraction = (Math.log(p) - Math.log(x[low])) / (Math.log(x[high]) - Math.log(x[low]));
        return y[low] + fraction * (y[high] - y[low]);
    }

    private static double[] array(List<Double> values) {
        double[] result = new double[values.size()];
        for (int i = 0; i < result.length; i++) result[i] = values.get(i);
        return result;
    }
}
