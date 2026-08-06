package edu.cnu.bopit.ui.study;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.persistence.BopitJsonPersistence;
import edu.cnu.bopit.study.LinearParameterAxis;
import edu.cnu.bopit.study.LogarithmicParameterAxis;
import edu.cnu.bopit.study.NamedStudyTemplates;
import edu.cnu.bopit.study.ParameterAxis;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterTarget;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.mdi.sim.ProgressInfo;
import edu.cnu.mdi.sim.SimulationContext;
import edu.cnu.mdi.sim.SimulationEngine;
import edu.cnu.mdi.sim.SimulationEngineConfig;
import edu.cnu.mdi.sim.SimulationListener;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Configures and runs named or custom one-/two-axis studies. */
public final class ParameterStudyWorkbenchView extends BaseView implements SimulationListener {
    private enum Template {
        CUSTOM, GRID_CONVERGENCE, CUTOFF_STABILITY, ADAPTIVE_VS_LEGACY,
        REGION_BIAS, STRONG_SENSITIVITY, FINITE_SIZE, WAVE_EQUATION
    }
    private enum Spacing { LINEAR, LOGARITHMIC }

    private final BopitProblem base;
    private final PhysicalConstantSet constants;
    private final JComboBox<Template> template = new JComboBox<>(Template.values());
    private final AxisControls first = new AxisControls(ParameterTarget.TOTAL_GRID_POINTS, 20, 80, 4);
    private final AxisControls second = new AxisControls(ParameterTarget.MAXIMUM_MOMENTUM_FM_INVERSE,
            100, 100_000, 5);
    private final JCheckBox useSecond = new JCheckBox("Enable second axis");
    private final JComboBox<StudyObservable> plottedObservable =
            new JComboBox<>(StudyObservable.values());
    private final JButton run = new JButton("Run study");
    private final JButton cancel = new JButton("Cancel");
    private final JProgressBar progress = new JProgressBar(0, 1000);
    private final JLabel status = new JLabel("Ready");
    private volatile SimulationEngine engine;
    private ParameterStudySimulation simulation;
    private ParameterStudy loadedStudy;

    public ParameterStudyWorkbenchView(BopitProblem base, PhysicalConstantSet constants) {
        super(PropertyUtils.TITLE, "Parameter Study",
                PropertyUtils.WIDTH, 820, PropertyUtils.HEIGHT, 520,
                PropertyUtils.USECONTAINER, false);
        this.base = base;
        this.constants = constants;
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Template")); north.add(template);
        north.add(new JLabel("Plot")); north.add(plottedObservable);
        JButton open = new JButton("Open…");
        JButton save = new JButton("Save…");
        open.addActionListener(event -> openStudy());
        save.addActionListener(event -> saveStudy());
        north.add(open); north.add(save);
        getContentPane().add(north, BorderLayout.NORTH);
        JPanel axes = new JPanel(new GridLayout(2, 1, 4, 4));
        axes.setBorder(BorderFactory.createTitledBorder("Custom axes"));
        axes.add(first.panel("Axis 1"));
        JPanel secondPanel = second.panel("Axis 2");
        secondPanel.add(useSecond);
        axes.add(secondPanel);
        getContentPane().add(axes, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(8, 4));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(run); buttons.add(cancel); buttons.add(status);
        south.add(buttons, BorderLayout.CENTER);
        progress.setStringPainted(true);
        south.add(progress, BorderLayout.SOUTH);
        getContentPane().add(south, BorderLayout.SOUTH);
        run.addActionListener(event -> runStudy());
        cancel.addActionListener(event -> { if (engine != null) engine.requestCancel(); });
        cancel.setEnabled(false);
    }

    /** Open a view whose Run action reproduces an already loaded definition. */
    public ParameterStudyWorkbenchView(ParameterStudy study, PhysicalConstantSet constants) {
        this(study.baseProblem(), constants);
        loadedStudy = study;
        template.setEnabled(false);
        useSecond.setEnabled(false);
        first.setEnabled(false);
        second.setEnabled(false);
        plottedObservable.setSelectedItem(study.observables().get(0));
        status.setText("Loaded study: " + study.name());
    }

    private void runStudy() {
        ParameterStudy study;
        try { study = createStudy(); }
        catch (RuntimeException failure) { status.setText("Invalid study: " + failure.getMessage()); return; }
        simulation = new ParameterStudySimulation(study, constants);
        engine = new SimulationEngine(simulation, new SimulationEngineConfig(0, 0, 0, true));
        simulation.bindEngine(engine);
        engine.addListener(this);
        run.setEnabled(false); cancel.setEnabled(true);
        engine.start();
    }

    private ParameterStudy createStudy() {
        if (loadedStudy != null) return loadedStudy;
        return switch ((Template) template.getSelectedItem()) {
            case GRID_CONVERGENCE -> NamedStudyTemplates.gridConvergence(base);
            case CUTOFF_STABILITY -> NamedStudyTemplates.cutoffStability(base);
            case ADAPTIVE_VS_LEGACY -> NamedStudyTemplates.adaptiveVersusLegacy(base);
            case REGION_BIAS -> NamedStudyTemplates.regionBias(base);
            case STRONG_SENSITIVITY -> NamedStudyTemplates.strongPotentialSensitivity(base);
            case FINITE_SIZE -> NamedStudyTemplates.finiteSizeSensitivity(base);
            case WAVE_EQUATION -> NamedStudyTemplates.waveEquationComparison(base);
            case CUSTOM -> {
                List<ParameterAxis> axes = useSecond.isSelected()
                        ? List.of(first.axis("Axis 1"), second.axis("Axis 2"))
                        : List.of(first.axis("Axis 1"));
                yield new ParameterStudy("Custom study", base, axes,
                        List.of((StudyObservable) plottedObservable.getSelectedItem(),
                                StudyObservable.ITERATIONS, StudyObservable.RESIDUAL));
            }
        };
    }

    private void saveStudy() {
        try {
            ParameterStudy study = createStudy();
            JFileChooser chooser = chooser("Save BOPIT study", "bopit-study.json");
            if (chooser.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
            java.nio.file.Path path = jsonExtension(chooser.getSelectedFile().toPath());
            BopitJsonPersistence.writeStudy(study, path);
            status.setText("Saved " + path.getFileName());
        } catch (Exception error) { showError("Could not save study", error); }
    }

    private void openStudy() {
        JFileChooser chooser = chooser("Open BOPIT study", "bopit-study.json");
        if (chooser.showOpenDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
        try {
            new ParameterStudyWorkbenchView(BopitJsonPersistence.readStudy(
                    chooser.getSelectedFile().toPath()), constants);
            status.setText("Opened " + chooser.getSelectedFile().getName());
        } catch (Exception error) { showError("Could not open study", error); }
    }

    private static JFileChooser chooser(String title, String filename) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter("BOPIT JSON files", "json"));
        chooser.setSelectedFile(new java.io.File(filename));
        return chooser;
    }

    private static java.nio.file.Path jsonExtension(java.nio.file.Path path) {
        return path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".json")
                ? path : path.resolveSibling(path.getFileName() + ".json");
    }

    private void showError(String title, Exception error) {
        status.setText(title + ": " + error.getMessage());
        JOptionPane.showMessageDialog(getContentPane(), error.getMessage(), title,
                JOptionPane.ERROR_MESSAGE);
    }

    @Override public void onProgress(SimulationContext context, ProgressInfo info) {
        progress.setValue((int) Math.round(1000 * info.fraction));
        progress.setString(info.message);
    }
    @Override public void onMessage(SimulationContext context, String message) { status.setText(message); }
    @Override public void onDone(SimulationContext context) {
        finish(false);
        new ParameterStudyResultView(simulation.result(false),
                (StudyObservable) plottedObservable.getSelectedItem());
    }
    @Override public void onFail(SimulationContext context, Throwable error) {
        status.setText("Study failed: " + error.getMessage()); run.setEnabled(true); cancel.setEnabled(false);
    }
    @Override public void onStateChange(SimulationContext context,
            edu.cnu.mdi.sim.SimulationState from, edu.cnu.mdi.sim.SimulationState to, String reason) {
        if (to == edu.cnu.mdi.sim.SimulationState.TERMINATED
                && context.isCancelRequested()) finish(true);
    }
    private void finish(boolean cancelled) {
        run.setEnabled(true); cancel.setEnabled(false);
        status.setText(cancelled ? "Study cancelled; completed points retained" : "Study complete");
        if (cancelled && simulation != null && !simulation.result(true).points().isEmpty()) {
            new ParameterStudyResultView(simulation.result(true),
                    (StudyObservable) plottedObservable.getSelectedItem());
        }
    }

    private static final class AxisControls {
        private final JComboBox<ParameterTarget> target = new JComboBox<>(ParameterTarget.values());
        private final JComboBox<Spacing> spacing = new JComboBox<>(Spacing.values());
        private final JSpinner minimum;
        private final JSpinner maximum;
        private final JSpinner count;
        AxisControls(ParameterTarget initial, double min, double max, int count) {
            target.setSelectedItem(initial);
            minimum = new JSpinner(new SpinnerNumberModel(min, -1e9, 1e9, 1.0));
            maximum = new JSpinner(new SpinnerNumberModel(max, -1e9, 1e9, 1.0));
            this.count = new JSpinner(new SpinnerNumberModel(count, 1, 100, 1));
        }
        JPanel panel(String title) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(BorderFactory.createTitledBorder(title));
            panel.add(target); panel.add(spacing); panel.add(new JLabel("min")); panel.add(minimum);
            panel.add(new JLabel("max")); panel.add(maximum); panel.add(new JLabel("count")); panel.add(count);
            return panel;
        }
        ParameterAxis axis(String name) {
            double min = ((Number) minimum.getValue()).doubleValue();
            double max = ((Number) maximum.getValue()).doubleValue();
            int n = ((Number) count.getValue()).intValue();
            return spacing.getSelectedItem() == Spacing.LOGARITHMIC
                    ? new LogarithmicParameterAxis(name, (ParameterTarget) target.getSelectedItem(), min, max, n)
                    : new LinearParameterAxis(name, (ParameterTarget) target.getSelectedItem(), min, max, n);
        }
        void setEnabled(boolean enabled) {
            target.setEnabled(enabled); spacing.setEnabled(enabled); minimum.setEnabled(enabled);
            maximum.setEnabled(enabled); count.setEnabled(enabled);
        }
    }
}
