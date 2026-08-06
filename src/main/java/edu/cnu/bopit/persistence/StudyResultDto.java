package edu.cnu.bopit.persistence;

import java.util.List;
import java.util.Map;

/** Optional retained study output embedded alongside its reproducible definition. */
public record StudyResultDto(boolean cancelled, List<PointDto> points) {
    public StudyResultDto { points = List.copyOf(points); }
    public record PointDto(List<CoordinateDto> coordinates, String status,
            Map<String, Double> observables, String message) {
        public PointDto {
            coordinates = List.copyOf(coordinates);
            observables = Map.copyOf(observables);
        }
    }
    public record CoordinateDto(String axisName, String target, int index,
            String label, double value) { }
}
