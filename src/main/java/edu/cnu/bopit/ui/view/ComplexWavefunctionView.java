package edu.cnu.bopit.ui.view;

import java.awt.Color;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.calculation.StrongInteractionResult;
import edu.cnu.bopit.calculation.ComplexKleinGordonResult;
import edu.cnu.bopit.physics.wavefunction.ComplexWavefunctions;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.plot.PlotDeck;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters.AxisScale;
import edu.cnu.mdi.view.ViewPropertiesBuilder;
import edu.cnu.mdi.view.BaseView;

/** Real, imaginary, and magnitude plots for a complex Gamow-like state. */
public final class ComplexWavefunctionView extends BaseView {
    public ComplexWavefunctionView(StrongInteractionResult result) {
        this(result.wavefunctions());
    }

    public ComplexWavefunctionView(ComplexKleinGordonResult result) {
        this(result.wavefunctions());
    }

    private ComplexWavefunctionView(ComplexWavefunctions wavefunctions) {
        super(new ViewPropertiesBuilder().title("Complex Wavefunction Diagnostics")
                .width(850).height(650).useContainer(false).buildOptions());
        PlotDeck plots = new PlotDeck(true);
        var momentum = wavefunctions.momentum();
        plots.addPlot("Momentum space", plot(momentum.momentaFmInverse(),
                momentum.radialValuesFmThreeHalves(), "Complex momentum wavefunction",
                "Momentum p (fm^-1)", "phi_l(p) (fm^(3/2))", true));
        var coordinate = wavefunctions.coordinate();
        plots.addPlot("Coordinate space", plot(coordinate.radiiFm(),
                coordinate.radialValuesFmMinusThreeHalves(), "Complex coordinate wavefunction",
                "Radius r (fm)", "R_l(r) (fm^(-3/2))", false));
        getContentPane().add(plots);
    }

    private static PlotPanel plot(double[] x, Complex[] values, String title,
            String xLabel, String yLabel, boolean logarithmicX) {
        double[] real = new double[values.length];
        double[] imaginary = new double[values.length];
        double[] magnitude = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            real[i] = values[i].getReal();
            imaginary[i] = values[i].getImaginary();
            magnitude[i] = values[i].abs();
        }
        var data = PlotSupport.xyData("Real", "Imaginary", "Magnitude");
        PlotSupport.curve(data, "Real", x, real, new Color(25, 75, 160),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Imaginary", x, imaginary, new Color(190, 75, 30),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotSupport.curve(data, "Magnitude", x, magnitude, new Color(30, 135, 70),
                SymbolType.NOSYMBOL, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data, title, xLabel, yLabel);
        canvas.getParameters().setLegendDrawing(true).includeYZero(true);
        if (logarithmicX) canvas.getParameters().setXScale(AxisScale.LOG10);
        else canvas.getParameters().includeXZero(true);
        return new PlotPanel(canvas);
    }
}
