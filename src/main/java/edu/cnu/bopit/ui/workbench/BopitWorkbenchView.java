package edu.cnu.bopit.ui.workbench;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.ui.editor.AtomStateEditorPanel;
import edu.cnu.bopit.ui.editor.GridEditorPanel;
import edu.cnu.bopit.ui.editor.SolverEditorPanel;
import edu.cnu.bopit.ui.view.SummaryResultView;
import edu.cnu.mdi.sim.ProgressInfo;
import edu.cnu.mdi.sim.SimulationContext;
import edu.cnu.mdi.sim.SimulationEngine;
import edu.cnu.mdi.sim.SimulationEngineConfig;
import edu.cnu.mdi.sim.SimulationListener;
import edu.cnu.mdi.sim.SimulationState;
import edu.cnu.mdi.util.PropertyUtils;
import edu.cnu.mdi.view.BaseView;

/** Stage 3 workbench for configuring and running the validated vertical slice. */
public final class BopitWorkbenchView extends BaseView implements SimulationListener {
    private static final Color ERROR_COLOR = new Color(150, 20, 20);
    private static final Color OK_COLOR = new Color(20, 105, 45);
    private static final String[] SECTIONS = { "Atom and state", "Momentum grid", "Solver" };

    private final AtomStateEditorPanel atomEditor = new AtomStateEditorPanel(this::refreshValidation);
    private final GridEditorPanel gridEditor = new GridEditorPanel(this::refreshValidation);
    private final SolverEditorPanel solverEditor = new SolverEditorPanel(this::refreshValidation);
    private final JTextArea validationText = new JTextArea(4, 60);
    private final JLabel status = new JLabel("Ready");
    private final JProgressBar progress = new JProgressBar(0, 1000);
    private final JButton runButton = new JButton("Run");
    private final JButton cancelButton = new JButton("Cancel");

    private volatile SimulationEngine currentEngine;
    private BopitCalculationSimulation currentSimulation;
    private BopitProblem submittedProblem;

    public BopitWorkbenchView() {
        super(PropertyUtils.TITLE, "Point-Coulomb Workbench",
                PropertyUtils.WIDTH, 920,
                PropertyUtils.HEIGHT, 690,
                PropertyUtils.USECONTAINER, false);
        getContentPane().add(createToolbar(), BorderLayout.NORTH);
        getContentPane().add(createEditorArea(), BorderLayout.CENTER);
        getContentPane().add(createStatusArea(), BorderLayout.SOUTH);
        loadSulfurPreset();
        cancelButton.setEnabled(false);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton presetButton = new JButton("Sulfur-32 3d preset");
        presetButton.addActionListener(event -> loadSulfurPreset());
        runButton.addActionListener(event -> runCalculation());
        cancelButton.addActionListener(event -> cancelCalculation());
        toolbar.add(presetButton);
        toolbar.add(runButton);
        toolbar.add(cancelButton);
        return toolbar;
    }

    private JSplitPane createEditorArea() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String section : SECTIONS) listModel.addElement(section);
        JList<String> sectionList = new JList<>(listModel);
        sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sectionList.setPreferredSize(new Dimension(170, 400));

        JPanel cards = new JPanel(new CardLayout());
        cards.add(atomEditor, SECTIONS[0]);
        cards.add(gridEditor, SECTIONS[1]);
        cards.add(solverEditor, SECTIONS[2]);
        sectionList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && sectionList.getSelectedValue() != null) {
                ((CardLayout) cards.getLayout()).show(cards, sectionList.getSelectedValue());
            }
        });
        sectionList.setSelectedIndex(0);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(sectionList), cards);
        split.setResizeWeight(0.2);
        return split;
    }

    private JPanel createStatusArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Validation and calculation status"),
                BorderFactory.createEmptyBorder(3, 6, 6, 6)));
        validationText.setEditable(false);
        validationText.setLineWrap(true);
        validationText.setWrapStyleWord(true);
        panel.add(new JScrollPane(validationText), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.add(status, BorderLayout.CENTER);
        progress.setStringPainted(true);
        progress.setPreferredSize(new Dimension(280, progress.getPreferredSize().height));
        bottom.add(progress, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void loadSulfurPreset() {
        BopitProblem preset = PublishedProblems.kaonicSulfur32Legacy3d(
                PublishedConstantSets.BOPIT_1990, 40, 10);
        atomEditor.load(preset);
        gridEditor.load(preset);
        solverEditor.load(preset);
        status.setText("Loaded published kaonic sulfur-32 3d legacy-grid preset");
        refreshValidation();
    }

    private WorkbenchProblemInput input() {
        var atom = atomEditor.values();
        var grid = gridEditor.values();
        var solver = solverEditor.values();
        return new WorkbenchProblemInput(atom.nuclearCharge(), atom.massNumber(),
                atom.particleMassMeV(), atom.nuclearMassMeV(), atom.principalN(), atom.orbitalL(),
                grid.gridKind(), grid.totalPoints(), grid.nuclearPoints(),
                grid.legacyAtomicScaleFmInverse(), grid.legacyNuclearScaleFmInverse(),
                grid.legacyBoundaryFmInverse(), grid.legacyMaximumMomentumScale(),
                grid.adaptiveMaximumMomentumFmInverse(), grid.adaptiveRegionBias(),
                solver.shiftMeV(), solver.energyToleranceMeV(), solver.residualTolerance(),
                solver.minimumIterations(), solver.maximumIterations());
    }

    private void refreshValidation() {
        if (validationText == null) return;
        ValidationReport report = input().validate();
        List<String> messages = report.isValid()
                ? (report.warnings().isEmpty() ? List.of("Input is valid.") : report.warnings())
                : report.errors();
        validationText.setText(String.join("\n", messages));
        validationText.setForeground(report.isValid() ? OK_COLOR : ERROR_COLOR);
        runButton.setEnabled(report.isValid() && !isCalculationActive());
    }

    private void runCalculation() {
        WorkbenchProblemInput editorInput = input();
        ValidationReport report = editorInput.validate();
        if (!report.isValid() || isCalculationActive()) return;
        submittedProblem = editorInput.toProblem();
        currentSimulation = new BopitCalculationSimulation(submittedProblem,
                PublishedConstantSets.BOPIT_1990);
        SimulationEngine engine = new SimulationEngine(currentSimulation,
                new SimulationEngineConfig(0, 0, 0, true));
        currentSimulation.bindEngine(engine);
        engine.addListener(this);
        currentEngine = engine;
        setRunningUi(true);
        progress.setIndeterminate(true);
        status.setText("Starting calculation…");
        engine.start();
    }

    private void cancelCalculation() {
        SimulationEngine engine = currentEngine;
        if (engine != null) {
            status.setText("Cancellation requested…");
            engine.requestCancel();
        }
    }

    private boolean isCalculationActive() {
        SimulationEngine engine = currentEngine;
        if (engine == null) return false;
        SimulationState state = engine.getState();
        return state != SimulationState.TERMINATED && state != SimulationState.FAILED;
    }

    private void setRunningUi(boolean running) {
        runButton.setEnabled(!running && input().validate().isValid());
        cancelButton.setEnabled(running);
    }

    @Override
    public void onProgress(SimulationContext context, ProgressInfo info) {
        progress.setIndeterminate(info.indeterminate);
        if (!info.indeterminate) progress.setValue((int) Math.round(1000.0 * info.fraction));
        if (info.message != null) progress.setString(info.message);
    }

    @Override
    public void onMessage(SimulationContext context, String message) {
        status.setText(message);
    }

    @Override
    public void onDone(SimulationContext context) {
        finishProgress();
        PointCoulombResult result = currentSimulation.result().orElse(null);
        if (result != null) {
            status.setText("Calculation complete; result retained in a new summary view");
            new SummaryResultView(submittedProblem, result, context.getElapsedSeconds());
        }
        setRunningUi(false);
    }

    @Override
    public void onFail(SimulationContext context, Throwable error) {
        finishProgress();
        status.setText("Calculation failed: " + error.getMessage());
        validationText.setForeground(ERROR_COLOR);
        validationText.setText("Calculation failed: " + error);
        setRunningUi(false);
    }

    @Override
    public void onStateChange(SimulationContext context, SimulationState from,
            SimulationState to, String reason) {
        if (to == SimulationState.TERMINATED && currentSimulation.result().isEmpty()) {
            finishProgress();
            status.setText("Calculation cancelled");
            setRunningUi(false);
        }
    }

    private void finishProgress() {
        progress.setIndeterminate(false);
        progress.setValue(1000);
    }

    @Override
    public void prepareForExit() {
        if (isCalculationActive()) cancelCalculation();
        super.prepareForExit();
    }
}
