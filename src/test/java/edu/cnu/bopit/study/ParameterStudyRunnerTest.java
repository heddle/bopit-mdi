package edu.cnu.bopit.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.InverseIterationSpec;

class ParameterStudyRunnerTest {
    @Test
    void runsSequentiallyAndPreservesInvalidPoint() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var axis = new ExplicitParameterAxis("points", ParameterTarget.TOTAL_GRID_POINTS,
                List.of(new AxisValue("20", 20), new AxisValue("21 invalid", 21),
                        new AxisValue("40", 40)));
        var study = new ParameterStudy("test", base, List.of(axis),
                List.of(StudyObservable.BINDING_ENERGY_MEV));
        var result = new ParameterStudyRunner().run(study, constants, CalculationMonitor.NONE);
        assertFalse(result.cancelled());
        assertEquals(3, result.points().size());
        assertEquals(StudyPointStatus.SUCCESS, result.points().get(0).status());
        assertEquals(StudyPointStatus.FAILED, result.points().get(1).status());
        assertEquals(StudyPointStatus.SUCCESS, result.points().get(2).status());
        assertTrue(result.points().get(1).message().contains("IllegalArgumentException"));
    }

    @Test
    void createsCartesianCoordinatesForTwoAxes() {
        var a = new ExplicitParameterAxis("a", ParameterTarget.TOTAL_GRID_POINTS,
                List.of(new AxisValue("20", 20), new AxisValue("40", 40)));
        var b = new ExplicitParameterAxis("b", ParameterTarget.MAXIMUM_MOMENTUM_FM_INVERSE,
                List.of(new AxisValue("100", 100), new AxisValue("1000", 1000),
                        new AxisValue("10000", 10000)));
        var coordinates = ParameterStudyRunner.coordinates(List.of(a, b));
        assertEquals(6, coordinates.size());
        assertEquals(40.0, coordinates.get(5).get(0).value().value());
        assertEquals(10_000.0, coordinates.get(5).get(1).value().value());
    }

    @Test
    void preservesNonConvergedPointAndDoesNotMutateBaseProblem() {
        var constants = PublishedConstantSets.BOPIT_1990;
        var original = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var base = new BopitProblem(original.atomicSystem(), original.quantumState(),
                original.waveEquation(), original.grid(),
                new InverseIterationSpec(-0.37, 1e-30, 1e-30, 1, 1),
                original.electromagnetic(), original.strongInteraction());
        var axis = new ExplicitParameterAxis("points", ParameterTarget.TOTAL_GRID_POINTS,
                List.of(new AxisValue("20", 20)));
        var study = new ParameterStudy("nonconvergence", base, List.of(axis),
                List.of(StudyObservable.BINDING_ENERGY_MEV));
        var result = new ParameterStudyRunner().run(study, constants, CalculationMonitor.NONE);
        assertEquals(StudyPointStatus.NON_CONVERGED, result.points().get(0).status());
        assertEquals(40, base.grid().totalPoints());
        assertTrue(result.points().get(0).observables().containsKey(
                StudyObservable.BINDING_ENERGY_MEV));
    }
}
