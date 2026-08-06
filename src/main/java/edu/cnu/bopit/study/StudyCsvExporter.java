package edu.cnu.bopit.study;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** RFC-4180-style CSV export for retained study results. */
public final class StudyCsvExporter {
    private StudyCsvExporter() { }

    public static void write(ParameterStudyResult result, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            write(result, writer);
        }
    }

    public static void write(ParameterStudyResult result, Writer writer) throws IOException {
        ParameterStudy study = result.study();
        for (ParameterAxis axis : study.axes()) {
            cell(writer, axis.name() + " label"); writer.write(',');
            cell(writer, axis.name() + " value"); writer.write(',');
        }
        cell(writer, "status"); writer.write(',');
        cell(writer, "message");
        for (StudyObservable observable : study.observables()) {
            writer.write(',');
            cell(writer, observable.label() + (observable.units().isBlank()
                    ? "" : " (" + observable.units() + ")"));
        }
        writer.write("\r\n");
        for (StudyPointResult point : result.points()) {
            for (StudyCoordinate coordinate : point.coordinates()) {
                cell(writer, coordinate.value().label()); writer.write(',');
                writer.write(Double.toString(coordinate.value().value())); writer.write(',');
            }
            cell(writer, point.status().name()); writer.write(',');
            cell(writer, point.message());
            for (StudyObservable observable : study.observables()) {
                writer.write(',');
                Double value = point.observables().get(observable);
                if (value != null) writer.write(Double.toString(value));
            }
            writer.write("\r\n");
        }
    }

    private static void cell(Writer writer, String value) throws IOException {
        writer.write('"');
        writer.write(value.replace("\"", "\"\""));
        writer.write('"');
    }
}
