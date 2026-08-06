package edu.cnu.bopit.study;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class StudyCsvExporterTest {
    @Test
    void exportsCoordinatesFailuresAndObservables() throws Exception {
        var base = PublishedProblems.kaonicSulfur32Legacy3d(PublishedConstantSets.BOPIT_1990, 40, 10);
        var axis = new ExplicitParameterAxis("Grid, points", ParameterTarget.TOTAL_GRID_POINTS,
                List.of(new AxisValue("bad \"point\"", 21)));
        var study = new ParameterStudy("csv", base, List.of(axis),
                List.of(StudyObservable.BINDING_ENERGY_MEV));
        var coordinate = new StudyCoordinate(axis.name(), axis.target(), 0, axis.values().get(0));
        var result = new ParameterStudyResult(study,
                List.of(new StudyPointResult(List.of(coordinate), StudyPointStatus.FAILED,
                        Map.of(), "invalid, retained")), false);
        StringWriter writer = new StringWriter();
        StudyCsvExporter.write(result, writer);
        assertTrue(writer.toString().contains("\"Grid, points label\""));
        assertTrue(writer.toString().contains("\"bad \"\"point\"\"\""));
        assertTrue(writer.toString().contains("\"invalid, retained\""));
    }
}
