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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.ui.BopitFileTypes;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.persistence.BopitJsonPersistence;
import edu.cnu.bopit.study.LinearParameterAxis;
import edu.cnu.bopit.study.LogarithmicParameterAxis;
import edu.cnu.bopit.study.NamedStudyTemplates;
import edu.cnu.bopit.study.ParameterAxis;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.study.ParameterTarget;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.mdi.sim.ProgressInfo;
import edu.cnu.mdi.dialog.FileDialogs;
import edu.cnu.mdi.sim.task.BackgroundTasks;
import edu.cnu.mdi.sim.task.TaskHandle;
import edu.cnu.mdi.sim.task.TaskListener;
import edu.cnu.mdi.view.ViewPropertiesBuilder;
import edu.cnu.mdi.view.BaseView;

/** Configures and runs named or custom one-/two-axis studies. */
public final class ParameterStudyWorkbenchView extends BaseView
        implements TaskListener<ParameterStudyResult> {
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
    private volatile TaskHandle<ParameterStudyResult> task;
    private ParameterStudy loadedStudy;

    public ParameterStudyWorkbenchView(BopitProblem base, PhysicalConstantSet constants) {
        super(new ViewPropertiesBuilder().title("Parameter Study")
                .width(820).height(520).useContainer(false).buildOptions());
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
        cancel.addActionListener(event -> { if (task != null) task.cancel(); });
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
        task = BackgroundTasks.create(new ParameterStudyTask(study, constants));
        task.addListener(this);
        run.setEnabled(false); cancel.setEnabled(true);
        task.start();
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
            var selected = FileDialogs.saveFile(getContentPane(), "bopit-study",
                    "Save BOPIT study", "bopit-study.json", BopitFileTypes.JSON);
            if (selected.isEmpty()) return;
            java.nio.file.Path path = selected.orElseThrow();
            BopitJsonPersistence.writeStudy(study, path);
            status.setText("Saved " + path.getFileName());
        } catch (Exception error) { showError("Could not save study", error); }
    }

    private void openStudy() {
        var selected = FileDialogs.openFile(getContentPane(), "bopit-study",
                "Open BOPIT study", BopitFileTypes.JSON);
        if (selected.isEmpty()) return;
        try {
            new ParameterStudyWorkbenchView(BopitJsonPersistence.readStudy(
                    selected.orElseThrow()), constants);
            status.setText("Opened " + selected.orElseThrow().getFileName());
        } catch (Exception error) { showError("Could not open study", error); }
    }

    private void showError(String title, Exception error) {
        status.setText(title + ": " + error.getMessage());
        JOptionPane.showMessageDialog(getContentPane(), error.getMessage(), title,
                JOptionPane.ERROR_MESSAGE);
    }

    @Override public void onProgress(TaskHandle<ParameterStudyResult> source,
            ProgressInfo info) {
        progress.setValue((int) Math.round(1000 * info.fraction));
        progress.setString(info.message);
    }
    @Override public void onMessage(TaskHandle<ParameterStudyResult> source,
            String message) { status.setText(message); }
    @Override public void onSucceeded(TaskHandle<ParameterStudyResult> source,
            ParameterStudyResult result) {
        finish(false);
        new ParameterStudyResultView(result,
                (StudyObservable) plottedObservable.getSelectedItem());
    }
    @Override public void onFailed(TaskHandle<ParameterStudyResult> source,
            Throwable error) {
        status.setText("Study failed: " + error.getMessage()); run.setEnabled(true); cancel.setEnabled(false);
    }
    @Override public void onCancelled(TaskHandle<ParameterStudyResult> source) {
        finish(true);
    }
    private void finish(boolean cancelled) {
        run.setEnabled(true); cancel.setEnabled(false);
        status.setText(cancelled ? "Study cancelled; completed points retained" : "Study complete");
        var result = task == null ? null : task.getResult();
        if (cancelled && result != null && !result.points().isEmpty()) {
            new ParameterStudyResultView(result,
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
