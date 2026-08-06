package edu.cnu.bopit.ui.plot;

import java.awt.Color;

import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.Curve;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.pdata.PlotDataType;

/** Small helpers for constructing immutable-result diagnostic plots. */
public final class PlotSupport {
    private PlotSupport() { }

    public static PlotData xyData(String... curveNames) {
        try {
            return new PlotData(PlotDataType.XYXY, curveNames, null);
        } catch (PlotDataException exception) {
            throw new IllegalArgumentException("invalid plot-data definition", exception);
        }
    }

    public static Curve curve(PlotData data, String name, double[] x, double[] y,
            Color color, SymbolType symbol, CurveDrawingMethod drawingMethod) {
        Curve curve = (Curve) data.getCurve(name);
        curve.addAll(x, y);
        curve.getStyle().setLineColor(color);
        curve.getStyle().setBorderColor(color);
        curve.getStyle().setFillColor(color);
        curve.getStyle().setSymbolType(symbol);
        curve.getStyle().setSymbolSize(5);
        curve.getStyle().setLineWidth(1.6f);
        curve.setCurveDrawingMethod(drawingMethod);
        return curve;
    }
}
