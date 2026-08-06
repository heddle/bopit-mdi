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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.persistence.BopitJsonPersistence;
import edu.cnu.bopit.persistence.CalculationReport;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.ui.editor.AtomStateEditorPanel;
import edu.cnu.bopit.ui.editor.GridEditorPanel;
import edu.cnu.bopit.ui.editor.ElectromagneticEditorPanel;
import edu.cnu.bopit.ui.editor.SolverEditorPanel;
import edu.cnu.bopit.ui.editor.StrongInteractionEditorPanel;
import edu.cnu.bopit.ui.editor.WaveEquationEditorPanel;
import edu.cnu.bopit.ui.view.ConvergenceView;
import edu.cnu.bopit.ui.view.CoulombMatrixHeatmapView;
import edu.cnu.bopit.ui.view.LandeDiagnosticView;
import edu.cnu.bopit.ui.view.MomentumGridView;
import edu.cnu.bopit.ui.view.SummaryResultView;
import edu.cnu.bopit.ui.view.WavefunctionView;
import edu.cnu.bopit.ui.view.ComplexWavefunctionView;
import edu.cnu.bopit.ui.view.StrongInteractionSummaryView;
import edu.cnu.bopit.ui.view.KleinGordonSummaryView;
import edu.cnu.bopit.ui.view.ComplexKleinGordonSummaryView;
import edu.cnu.bopit.ui.view.DiracSummaryView;
import edu.cnu.bopit.ui.study.ParameterStudyWorkbenchView;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.OrbitingParticle;
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
    private static final String[] SECTIONS = {
            "Atom and state", "Wave equation", "Electromagnetism", "Strong interaction",
            "Momentum grid", "Solver" };

    private final AtomStateEditorPanel atomEditor = new AtomStateEditorPanel(this::refreshValidation);
    private final WaveEquationEditorPanel equationEditor =
            new WaveEquationEditorPanel(this::refreshValidation);
    private final ElectromagneticEditorPanel electromagneticEditor =
            new ElectromagneticEditorPanel(this::refreshValidation);
    private final StrongInteractionEditorPanel strongEditor =
            new StrongInteractionEditorPanel(this::refreshValidation);
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
    private PointCoulombResult completedPointResult;
    private PhysicalConstantSet calculationConstants = PublishedConstantSets.PDG_2024;

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
        JButton tableIIIButton = new JButton("Table III preset");
        JButton openButton = new JButton("Open problem…");
        JButton saveButton = new JButton("Save problem…");
        JButton reportButton = new JButton("Save report…");
        JButton studyButton = new JButton("Parameter study…");
        presetButton.addActionListener(event -> loadSulfurPreset());
        tableIIIButton.addActionListener(event -> loadTableIIIPreset());
        openButton.addActionListener(event -> openProblem());
        saveButton.addActionListener(event -> saveProblem());
        reportButton.addActionListener(event -> saveReport());
        studyButton.addActionListener(event -> openParameterStudy());
        runButton.addActionListener(event -> runCalculation());
        cancelButton.addActionListener(event -> cancelCalculation());
        toolbar.add(presetButton);
        toolbar.add(tableIIIButton);
        toolbar.add(openButton);
        toolbar.add(saveButton);
        toolbar.add(reportButton);
        toolbar.add(studyButton);
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
        cards.add(equationEditor, SECTIONS[1]);
        cards.add(electromagneticEditor, SECTIONS[2]);
        cards.add(strongEditor, SECTIONS[3]);
        cards.add(gridEditor, SECTIONS[4]);
        cards.add(solverEditor, SECTIONS[5]);
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
        calculationConstants = PublishedConstantSets.PDG_2024;
        BopitProblem preset = PublishedProblems.kaonicSulfur32Legacy3d(
                PublishedConstantSets.BOPIT_1990, 40, 10);
        atomEditor.load(preset);
        atomEditor.useRecommendedParticleMass();
        equationEditor.load(preset.waveEquation());
        gridEditor.load(preset);
        solverEditor.load(preset);
        electromagneticEditor.loadPointCharge();
        strongEditor.loadNone();
        completedPointResult = null;
        status.setText("Loaded sulfur-32 3d legacy-grid preset with PDG-2024 particle mass");
        refreshValidation();
    }

    private void loadTableIIIPreset() {
        calculationConstants = PublishedConstantSets.BOPIT_1990;
        loadProblem(PublishedProblems.kaonicSulfur32TableIII(
                calculationConstants, 80, 20));
        status.setText("Loaded Kwon-Tabakin Table III sulfur-32 3d regression preset");
    }

    private void loadProblem(BopitProblem problem) {
        atomEditor.load(problem);
        equationEditor.load(problem.waveEquation());
        gridEditor.load(problem);
        solverEditor.load(problem);
        electromagneticEditor.load(problem.electromagnetic());
        strongEditor.load(problem.strongInteraction());
        completedPointResult = null;
        refreshValidation();
    }

    private void openProblem() {
        JFileChooser chooser = jsonChooser("Open BOPIT problem", "bopit-problem.json");
        if (chooser.showOpenDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
        try {
            BopitProblem problem = BopitJsonPersistence.readProblem(chooser.getSelectedFile().toPath());
            calculationConstants = PublishedConstantSets.PDG_2024;
            loadProblem(problem);
            status.setText("Opened " + chooser.getSelectedFile().getName());
        } catch (Exception error) { showFileError("Could not open problem", error); }
    }

    private void saveProblem() {
        try {
            BopitProblem problem = configuredProblem(input());
            JFileChooser chooser = jsonChooser("Save BOPIT problem", "bopit-problem.json");
            if (chooser.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
            java.nio.file.Path path = extension(chooser.getSelectedFile().toPath(), ".json");
            BopitJsonPersistence.writeProblem(problem, path);
            status.setText("Saved " + path.getFileName());
        } catch (Exception error) { showFileError("Could not save problem", error); }
    }

    private void saveReport() {
        if (completedPointResult == null || submittedProblem == null) {
            status.setText("Run a point-Coulomb Schrödinger calculation before saving a report");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save calculation report");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        chooser.setSelectedFile(new java.io.File("bopit-calculation.txt"));
        if (chooser.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path path = extension(chooser.getSelectedFile().toPath(), ".txt");
        try {
            CalculationReport.write(submittedProblem, completedPointResult, path);
            status.setText("Saved " + path.getFileName());
        } catch (Exception error) { showFileError("Could not save report", error); }
    }

    private static JFileChooser jsonChooser(String title, String filename) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter("BOPIT JSON files", "json"));
        chooser.setSelectedFile(new java.io.File(filename));
        return chooser;
    }

    private static java.nio.file.Path extension(java.nio.file.Path path, String extension) {
        return path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(extension)
                ? path : path.resolveSibling(path.getFileName() + extension);
    }

    private void showFileError(String title, Exception error) {
        status.setText(title + ": " + error.getMessage());
        JOptionPane.showMessageDialog(getContentPane(), error.getMessage(), title,
                JOptionPane.ERROR_MESSAGE);
    }

    private WorkbenchProblemInput input() {
        var atom = atomEditor.values();
        var grid = gridEditor.values();
        var solver = solverEditor.values();
        return new WorkbenchProblemInput(atom.nuclearCharge(), atom.massNumber(),
                atom.particle(), atom.particleMassMeV(), atom.nuclearMassMeV(),
                atom.principalN(), atom.orbitalL(),
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
        List<String> electromagneticErrors = electromagneticEditor.validationErrors();
        if (!electromagneticErrors.isEmpty()) {
            java.util.ArrayList<String> errors = new java.util.ArrayList<>(report.errors());
            errors.addAll(electromagneticErrors);
            report = new ValidationReport(errors, report.warnings());
        }
        List<String> strongErrors = strongEditor.validationErrors();
        if (!strongErrors.isEmpty()) {
            java.util.ArrayList<String> errors = new java.util.ArrayList<>(report.errors());
            errors.addAll(strongErrors);
            report = new ValidationReport(errors, report.warnings());
        }
        if (strongErrors.isEmpty() && strongEditor.values() instanceof KwonTabakinOpticalPotentialSpec
                && atomEditor.values().particle() != OrbitingParticle.KAON_MINUS) {
            java.util.ArrayList<String> errors = new java.util.ArrayList<>(report.errors());
            errors.add("The Kwon-Tabakin strong interaction is a kaon-nucleus model and requires K-.");
            report = new ValidationReport(errors, report.warnings());
        }
        if (equationEditor.value() instanceof DiracSpec
                && atomEditor.values().particle().twiceSpin() != 1) {
            java.util.ArrayList<String> errors = new java.util.ArrayList<>(report.errors());
            errors.add("The Dirac equation requires a spin-1/2 orbiting particle (electron or muon).");
            report = new ValidationReport(errors, report.warnings());
        }
        if (strongErrors.isEmpty() && equationEditor.value() instanceof DiracSpec dirac) {
            int expectedL = dirac.kappa() < 0 ? -dirac.kappa() - 1 : dirac.kappa();
            java.util.ArrayList<String> errors = new java.util.ArrayList<>(report.errors());
            if (dirac.kappa() == 0) errors.add("Dirac kappa cannot be zero.");
            if (atomEditor.values().orbitalL() != expectedL) {
                errors.add("For Dirac kappa=" + dirac.kappa()
                        + ", orbital l must be " + expectedL + ".");
            }
            if (!(strongEditor.values() instanceof edu.cnu.bopit.model.NoStrongInteractionSpec)) {
                errors.add("The current Dirac implementation does not include a strong interaction.");
            }
            report = new ValidationReport(errors, report.warnings());
        }
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
        submittedProblem = configuredProblem(editorInput);
        currentSimulation = new BopitCalculationSimulation(submittedProblem,
                calculationConstants);
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

    private void openParameterStudy() {
        ValidationReport report = input().validate();
        if (!report.isValid() || !electromagneticEditor.validationErrors().isEmpty()
                || !strongEditor.validationErrors().isEmpty()) {
            status.setText("Correct invalid inputs before opening a parameter study");
            return;
        }
        new ParameterStudyWorkbenchView(configuredProblem(input()), calculationConstants);
    }

    private BopitProblem configuredProblem(WorkbenchProblemInput editorInput) {
        BopitProblem baseProblem = editorInput.toProblem();
        return new BopitProblem(baseProblem.atomicSystem(), baseProblem.quantumState(),
                equationEditor.value(), baseProblem.grid(), baseProblem.solver(),
                electromagneticEditor.values(), strongEditor.values());
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
            completedPointResult = result;
            status.setText("Calculation complete; retained result and diagnostic views opened");
            new SummaryResultView(submittedProblem, result, context.getElapsedSeconds());
            new MomentumGridView(submittedProblem, result, calculationConstants);
            new ConvergenceView(result);
            new LandeDiagnosticView(submittedProblem, result, calculationConstants);
            new CoulombMatrixHeatmapView(result);
            new WavefunctionView(result);
        } else if (currentSimulation.strongResult().isPresent()) {
            var strongResult = currentSimulation.strongResult().orElseThrow();
            status.setText("Complex strong-interaction calculation complete");
            new StrongInteractionSummaryView(submittedProblem, strongResult,
                    context.getElapsedSeconds());
            new ComplexWavefunctionView(strongResult);
        } else if (currentSimulation.kleinGordonResult().isPresent()) {
            var kleinGordon = currentSimulation.kleinGordonResult().orElseThrow();
            status.setText("Klein-Gordon calculation complete");
            new KleinGordonSummaryView(submittedProblem, kleinGordon,
                    context.getElapsedSeconds());
        } else if (currentSimulation.complexKleinGordonResult().isPresent()) {
            var complexKg = currentSimulation.complexKleinGordonResult().orElseThrow();
            status.setText("Complex Klein-Gordon calculation complete");
            new ComplexKleinGordonSummaryView(submittedProblem, complexKg,
                    context.getElapsedSeconds());
            new ComplexWavefunctionView(complexKg);
        } else if (currentSimulation.diracResult().isPresent()) {
            var dirac = currentSimulation.diracResult().orElseThrow();
            status.setText("Dirac calculation complete");
            new DiracSummaryView(submittedProblem, dirac, context.getElapsedSeconds());
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
        if (to == SimulationState.TERMINATED && currentSimulation.result().isEmpty()
                && currentSimulation.strongResult().isEmpty()
                && currentSimulation.kleinGordonResult().isEmpty()
                && currentSimulation.complexKleinGordonResult().isEmpty()
                && currentSimulation.diracResult().isEmpty()) {
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
