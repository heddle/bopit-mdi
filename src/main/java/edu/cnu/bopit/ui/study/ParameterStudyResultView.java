package edu.cnu.bopit.ui.study;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.persistence.BopitJsonPersistence;
import edu.cnu.bopit.study.StudyCsvExporter;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.bopit.study.StudyPointStatus;
import edu.cnu.bopit.ui.plot.PlotSupport;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.Histo2DData;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.plot.MultiplotPanel;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Sortable retained study table with line/heatmap visualization and CSV export. */
public final class ParameterStudyResultView extends BaseView {
    private final ParameterStudyResult result;
    private final JLabel message = new JLabel(" ");

    public ParameterStudyResultView(ParameterStudyResult result, StudyObservable requested) {
        super(PropertyUtils.TITLE, "Study: " + result.study().name(),
                PropertyUtils.WIDTH, 980, PropertyUtils.HEIGHT, 700,
                PropertyUtils.USECONTAINER, false);
        this.result = result;
        StudyObservable observable = result.study().observables().contains(requested)
                ? requested : result.study().observables().get(0);
        JTable table = new JTable(tableModel(result));
        table.setAutoCreateRowSorter(true);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table), plots(result, observable));
        split.setResizeWeight(0.5);
        getContentPane().add(split, BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton export = new JButton("Export CSV…");
        JButton saveJson = new JButton("Save result JSON…");
        export.addActionListener(event -> exportCsv());
        saveJson.addActionListener(event -> saveJson());
        controls.add(export);
        controls.add(saveJson);
        controls.add(new JLabel(result.cancelled() ? "Cancelled; partial results retained" : "Complete"));
        controls.add(message);
        getContentPane().add(controls, BorderLayout.SOUTH);
    }

    private static DefaultTableModel tableModel(ParameterStudyResult result) {
        List<String> columns = new ArrayList<>();
        result.study().axes().forEach(axis -> {
            columns.add(axis.name()); columns.add(axis.name() + " value");
        });
        columns.add("Status"); columns.add("Message");
        result.study().observables().forEach(observable -> columns.add(observable.label()
                + (observable.units().isBlank() ? "" : " (" + observable.units() + ")")));
        DefaultTableModel model = new DefaultTableModel(columns.toArray(), 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int column) {
                if (getRowCount() == 0) return Object.class;
                Object value = getValueAt(0, column);
                return value == null ? Object.class : value.getClass();
            }
        };
        for (var point : result.points()) {
            List<Object> row = new ArrayList<>();
            point.coordinates().forEach(coordinate -> {
                row.add(coordinate.value().label()); row.add(coordinate.value().value());
            });
            row.add(point.status()); row.add(point.message());
            result.study().observables().forEach(observable -> row.add(point.observables().get(observable)));
            model.addRow(row.toArray());
        }
        return model;
    }

    private static JPanel plots(ParameterStudyResult result, StudyObservable observable) {
        MultiplotPanel plots = new MultiplotPanel(true);
        if (result.study().axes().size() == 1) plots.addPlot(observable.label(), linePlot(result, observable));
        else plots.addPlot(observable.label(), heatmap(result, observable));
        return plots;
    }

    private static PlotPanel linePlot(ParameterStudyResult result, StudyObservable observable) {
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();
        for (var point : result.points()) {
            Double y = point.observables().get(observable);
            if (point.status() == StudyPointStatus.SUCCESS && y != null && Double.isFinite(y)) {
                xValues.add(point.coordinates().get(0).value().value()); yValues.add(y);
            }
        }
        double[] x = xValues.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yValues.stream().mapToDouble(Double::doubleValue).toArray();
        var data = PlotSupport.xyData(observable.label());
        PlotSupport.curve(data, observable.label(), x, y, new Color(25, 75, 160),
                SymbolType.CIRCLE, CurveDrawingMethod.CONNECT);
        PlotCanvas canvas = new PlotCanvas(data, result.study().name(),
                result.study().axes().get(0).name(), observable.label()
                        + (observable.units().isBlank() ? "" : " (" + observable.units() + ")"));
        canvas.getParameters().setLegendDrawing(true);
        return new PlotPanel(canvas);
    }

    private static PlotPanel heatmap(ParameterStudyResult result, StudyObservable observable) {
        int nx = result.study().axes().get(0).values().size();
        int ny = result.study().axes().get(1).values().size();
        Histo2DData histogram = new Histo2DData(observable.label(), 0, nx, nx, 0, ny, ny);
        for (var point : result.points()) {
            Double z = point.observables().get(observable);
            if (point.status() == StudyPointStatus.SUCCESS && z != null && Double.isFinite(z)) {
                histogram.fill(point.coordinates().get(0).index() + 0.5,
                        point.coordinates().get(1).index() + 0.5, z);
            }
        }
        try {
            PlotCanvas canvas = new PlotCanvas(new PlotData(histogram), result.study().name(),
                    result.study().axes().get(0).name(), result.study().axes().get(1).name());
            canvas.getParameters().setShowEmptyBins(true);
            return new PlotPanel(canvas);
        } catch (PlotDataException error) {
            throw new IllegalStateException("could not create study heatmap", error);
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export study CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setSelectedFile(new java.io.File("bopit-study.csv"));
        if (chooser.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            path = path.resolveSibling(path.getFileName() + ".csv");
        }
        try {
            StudyCsvExporter.write(result, path);
            message.setText("Exported " + path.getFileName());
        } catch (IOException error) {
            message.setText("Export failed: " + error.getMessage());
        }
    }

    private void saveJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save retained study result");
        chooser.setFileFilter(new FileNameExtensionFilter("BOPIT JSON files", "json"));
        chooser.setSelectedFile(new java.io.File("bopit-study-result.json"));
        if (chooser.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            path = path.resolveSibling(path.getFileName() + ".json");
        }
        try {
            BopitJsonPersistence.writeStudyResult(result, path);
            message.setText("Saved " + path.getFileName());
        } catch (IOException error) {
            message.setText("Save failed: " + error.getMessage());
        }
    }
}
