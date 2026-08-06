package edu.cnu.bopit.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.study.AxisValue;
import edu.cnu.bopit.study.ExplicitParameterAxis;
import edu.cnu.bopit.study.LinearParameterAxis;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.study.ParameterTarget;
import edu.cnu.bopit.study.StudyCoordinate;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.bopit.study.StudyPointResult;
import edu.cnu.bopit.study.StudyPointStatus;

class BopitJsonPersistenceTest {
    @TempDir Path directory;

    @Test void roundTripsEveryProblemSectionAndStoresVersionHeader() throws Exception {
        BopitProblem original = PublishedProblems.kaonicSulfur32Adaptive3d(
                PublishedConstantSets.BOPIT_1990, 40, 10);
        FermiChargeSpec fermi = new FermiChargeSpec(3.2, 0.59, 0.05);
        original = new BopitProblem(original.atomicSystem(), original.quantumState(),
                new KleinGordonSpec(KleinGordonForm.SCALAR_NUCLEAR, 2e-9, 19),
                original.grid(), original.solver(), new ElectromagneticSpec(fermi, true),
                new KwonTabakinOpticalPotentialSpec(new Complex(-0.44, 0.81), fermi));
        Path file = directory.resolve("problem.json");
        BopitJsonPersistence.writeProblem(original, file);
        assertEquals(original, BopitJsonPersistence.readProblem(file));
        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"format\": \"bopit-problem\""));
        assertTrue(json.contains("\"schemaVersion\": 1"));
        assertTrue(json.contains("\"applicationVersion\""));
    }

    @Test void roundTripsStudyDefinitionAndOptionalRetainedResult() throws Exception {
        BopitProblem problem = PublishedProblems.kaonicSulfur32Legacy3d(
                PublishedConstantSets.BOPIT_1990, 40, 10);
        ParameterStudy study = new ParameterStudy("Persistence test", problem, List.of(
                new LinearParameterAxis("Points", ParameterTarget.TOTAL_GRID_POINTS, 20, 40, 2),
                new ExplicitParameterAxis("Equation", ParameterTarget.WAVE_EQUATION,
                        List.of(new AxisValue("Schrodinger", 0), new AxisValue("Dirac", 6)))),
                List.of(StudyObservable.BINDING_ENERGY_MEV, StudyObservable.RESIDUAL));
        Path definition = directory.resolve("study.json");
        BopitJsonPersistence.writeStudy(study, definition);
        assertEquals(study, BopitJsonPersistence.readStudy(definition));

        StudyPointResult point = new StudyPointResult(List.of(
                new StudyCoordinate("Points", ParameterTarget.TOTAL_GRID_POINTS, 0,
                        new AxisValue("20", 20)),
                new StudyCoordinate("Equation", ParameterTarget.WAVE_EQUATION, 0,
                        new AxisValue("Schrodinger", 0))), StudyPointStatus.SUCCESS,
                Map.of(StudyObservable.BINDING_ENERGY_MEV, -0.36,
                        StudyObservable.RESIDUAL, Double.NaN), "converged");
        ParameterStudyResult original = new ParameterStudyResult(study, List.of(point), false);
        Path retained = directory.resolve("result.json");
        BopitJsonPersistence.writeStudyResult(original, retained);
        ParameterStudyResult restored = BopitJsonPersistence.readStudyResult(retained);
        assertEquals(study, restored.study());
        assertEquals(-0.36, restored.points().get(0).observables()
                .get(StudyObservable.BINDING_ENERGY_MEV));
        assertTrue(!restored.points().get(0).observables().containsKey(StudyObservable.RESIDUAL));
    }

    @Test void givesClearFailuresForNewerSchemaWrongFormatAndMalformedJson() throws Exception {
        Path newer = directory.resolve("newer.json");
        Files.writeString(newer, """
                {"header":{"format":"bopit-problem","schemaVersion":99,
                "applicationVersion":"future","created":"2026-01-01T00:00:00Z"},"problem":{}}
                """, StandardCharsets.UTF_8);
        assertTrue(assertThrows(PersistenceException.class,
                () -> BopitJsonPersistence.readProblem(newer)).getMessage().contains("newer"));

        Path wrong = directory.resolve("wrong.json");
        ParameterStudy study = new ParameterStudy("x",
                PublishedProblems.kaonicSulfur32Legacy3d(PublishedConstantSets.BOPIT_1990, 20, 6),
                List.of(new LinearParameterAxis("x", ParameterTarget.TOTAL_GRID_POINTS, 20, 20, 1)),
                List.of(StudyObservable.ITERATIONS));
        BopitJsonPersistence.writeStudy(study, wrong);
        assertTrue(assertThrows(PersistenceException.class,
                () -> BopitJsonPersistence.readProblem(wrong)).getMessage().contains("Expected format"));

        Path malformed = directory.resolve("bad.json");
        Files.writeString(malformed, "{ definitely not JSON", StandardCharsets.UTF_8);
        assertTrue(assertThrows(PersistenceException.class,
                () -> BopitJsonPersistence.readProblem(malformed)).getMessage().contains("Malformed JSON"));
    }

    @Test void writesHumanReadableCalculationReportWithUnitsAndConvergence() throws Exception {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Legacy3d(constants, 20, 6);
        var result = new PointCoulombCalculator().calculate(problem, constants, CalculationMonitor.NONE);
        Path report = directory.resolve("report.txt");
        CalculationReport.write(problem, result, report);
        String text = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(text.contains("Calculated binding energy"));
        assertTrue(text.contains("Reference Coulomb energy"));
        assertTrue(text.contains("MeV"));
        assertTrue(text.contains("Final residual"));
        assertTrue(text.contains("legacy"));
    }
}
