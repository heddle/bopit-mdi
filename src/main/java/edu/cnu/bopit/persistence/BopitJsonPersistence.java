package edu.cnu.bopit.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.cnu.bopit.BopitVersion;
import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ChargeDistributionSpec;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.GaussianChargeSpec;
import edu.cnu.bopit.model.GridSpec;
import edu.cnu.bopit.model.InverseIterationSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.model.PointChargeSpec;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.model.StrongInteractionSpec;
import edu.cnu.bopit.model.UniformChargeSpec;
import edu.cnu.bopit.model.WaveEquationSpec;
import edu.cnu.bopit.study.AxisValue;
import edu.cnu.bopit.study.ExplicitParameterAxis;
import edu.cnu.bopit.study.LinearParameterAxis;
import edu.cnu.bopit.study.LogarithmicParameterAxis;
import edu.cnu.bopit.study.ParameterAxis;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.study.ParameterTarget;
import edu.cnu.bopit.study.StudyCoordinate;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.bopit.study.StudyPointResult;
import edu.cnu.bopit.study.StudyPointStatus;

/** Reads and writes explicit schema-versioned BOPIT JSON documents. */
public final class BopitJsonPersistence {
    public static final int SCHEMA_VERSION = 1;
    public static final String PROBLEM_FORMAT = "bopit-problem";
    public static final String STUDY_FORMAT = "bopit-study";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                @Override public void write(JsonWriter out, Instant value) throws IOException {
                    out.value(value.toString());
                }
                @Override public Instant read(JsonReader in) throws IOException {
                    return Instant.parse(in.nextString());
                }
            }).setPrettyPrinting().disableHtmlEscaping().create();

    private BopitJsonPersistence() { }

    public static void writeProblem(BopitProblem problem, Path path) throws IOException {
        write(new ProblemDocument(header(PROBLEM_FORMAT), toDto(problem)), path);
    }

    public static BopitProblem readProblem(Path path) throws IOException, PersistenceException {
        ProblemDocument document = readDocument(path, ProblemDocument.class, PROBLEM_FORMAT);
        try { return fromDto(document.problem()); }
        catch (RuntimeException error) {
            throw new PersistenceException("Invalid problem definition: " + message(error), error);
        }
    }

    public static void writeStudy(ParameterStudy study, Path path) throws IOException {
        write(new StudyDocument(header(STUDY_FORMAT), toDto(study), null), path);
    }

    public static void writeStudyResult(ParameterStudyResult result, Path path) throws IOException {
        write(new StudyDocument(header(STUDY_FORMAT), toDto(result.study()), toDto(result)), path);
    }

    public static StudyDocument readStudyDocument(Path path) throws IOException, PersistenceException {
        StudyDocument document = readDocument(path, StudyDocument.class, STUDY_FORMAT);
        if (document.study() == null) throw new PersistenceException("Study file has no study definition.");
        return document;
    }

    public static ParameterStudy readStudy(Path path) throws IOException, PersistenceException {
        try { return fromDto(readStudyDocument(path).study()); }
        catch (PersistenceException error) { throw error; }
        catch (RuntimeException error) {
            throw new PersistenceException("Invalid study definition: " + message(error), error);
        }
    }

    public static ParameterStudyResult readStudyResult(Path path)
            throws IOException, PersistenceException {
        StudyDocument document = readStudyDocument(path);
        if (document.result() == null) throw new PersistenceException("Study file contains no retained result.");
        try {
            ParameterStudy study = fromDto(document.study());
            List<StudyPointResult> points = new ArrayList<>();
            for (var point : document.result().points()) {
                List<StudyCoordinate> coordinates = point.coordinates().stream().map(coordinate ->
                        new StudyCoordinate(coordinate.axisName(), enumValue(ParameterTarget.class,
                                coordinate.target(), "coordinate target"), coordinate.index(),
                                new AxisValue(coordinate.label(), coordinate.value()))).toList();
                Map<StudyObservable, Double> observables = new EnumMap<>(StudyObservable.class);
                point.observables().forEach((key, value) -> observables.put(
                        enumValue(StudyObservable.class, key, "result observable"), value));
                points.add(new StudyPointResult(coordinates,
                        enumValue(StudyPointStatus.class, point.status(), "point status"),
                        observables, point.message()));
            }
            return new ParameterStudyResult(study, points, document.result().cancelled());
        } catch (RuntimeException error) {
            throw new PersistenceException("Invalid retained study result: " + message(error), error);
        }
    }

    static ProblemDto toDto(BopitProblem problem) {
        AtomicSystem a = problem.atomicSystem();
        Map<String, Object> atom = map("nuclearCharge", a.nuclearCharge(), "massNumber", a.massNumber(),
                "particle", a.particle().name(), "particleMassMeV", a.particleMassMeV(),
                "nuclearMassMeV", a.nuclearMassMeV());
        Map<String, Object> state = map("principalN", problem.quantumState().principalN(),
                "orbitalL", problem.quantumState().orbitalL());
        return new ProblemDto(atom, state, wave(problem.waveEquation()), grid(problem.grid()),
                solver(problem.solver()), electromagnetic(problem.electromagnetic()),
                strong(problem.strongInteraction()));
    }

    static BopitProblem fromDto(ProblemDto dto) {
        if (dto == null) throw new IllegalArgumentException("problem section is missing");
        Map<String, Object> a = dto.atom();
        AtomicSystem atom = new AtomicSystem(integer(a, "nuclearCharge"), integer(a, "massNumber"),
                enumValue(OrbitingParticle.class, string(a, "particle"), "particle"),
                number(a, "particleMassMeV"), number(a, "nuclearMassMeV"));
        QuantumState state = new QuantumState(integer(dto.state(), "principalN"),
                integer(dto.state(), "orbitalL"));
        return new BopitProblem(atom, state, wave(dto.waveEquation()), grid(dto.grid()),
                solver(dto.solver()), electromagnetic(dto.electromagnetic()),
                strong(dto.strongInteraction()));
    }

    private static StudyDto toDto(ParameterStudy study) {
        List<AxisDto> axes = study.axes().stream().map(BopitJsonPersistence::toDto).toList();
        return new StudyDto(study.name(), toDto(study.baseProblem()), axes,
                study.observables().stream().map(Enum::name).toList());
    }

    private static ParameterStudy fromDto(StudyDto dto) {
        List<ParameterAxis> axes = dto.axes().stream().map(BopitJsonPersistence::fromDto).toList();
        List<StudyObservable> observables = dto.observables().stream().map(name ->
                enumValue(StudyObservable.class, name, "study observable")).toList();
        return new ParameterStudy(dto.name(), fromDto(dto.baseProblem()), axes, observables);
    }

    private static AxisDto toDto(ParameterAxis axis) {
        if (axis instanceof LinearParameterAxis value) return new AxisDto("linear", value.name(),
                value.target().name(), value.minimum(), value.maximum(), value.count(), null);
        if (axis instanceof LogarithmicParameterAxis value) return new AxisDto("logarithmic", value.name(),
                value.target().name(), value.minimum(), value.maximum(), value.count(), null);
        ExplicitParameterAxis value = (ExplicitParameterAxis) axis;
        return new AxisDto("explicit", value.name(), value.target().name(), null, null, null,
                value.values().stream().map(v -> new AxisDto.ValueDto(v.label(), v.value())).toList());
    }

    private static ParameterAxis fromDto(AxisDto dto) {
        ParameterTarget target = enumValue(ParameterTarget.class, dto.target(), "axis target");
        return switch (required(dto.type(), "axis type")) {
            case "linear" -> new LinearParameterAxis(dto.name(), target,
                    required(dto.minimum(), "axis minimum"), required(dto.maximum(), "axis maximum"),
                    required(dto.count(), "axis count"));
            case "logarithmic" -> new LogarithmicParameterAxis(dto.name(), target,
                    required(dto.minimum(), "axis minimum"), required(dto.maximum(), "axis maximum"),
                    required(dto.count(), "axis count"));
            case "explicit" -> new ExplicitParameterAxis(dto.name(), target,
                    required(dto.values(), "axis values").stream()
                            .map(v -> new AxisValue(v.label(), v.value())).toList());
            default -> throw new IllegalArgumentException("unsupported axis type '" + dto.type() + "'");
        };
    }

    private static StudyResultDto toDto(ParameterStudyResult result) {
        return new StudyResultDto(result.cancelled(), result.points().stream().map(point -> {
            List<StudyResultDto.CoordinateDto> coordinates = point.coordinates().stream().map(c ->
                    new StudyResultDto.CoordinateDto(c.axisName(), c.target().name(), c.index(),
                            c.value().label(), c.value().value())).toList();
            Map<String, Double> observables = new LinkedHashMap<>();
            point.observables().forEach((key, value) -> {
                if (value != null && Double.isFinite(value)) observables.put(key.name(), value);
            });
            return new StudyResultDto.PointDto(coordinates, point.status().name(), observables,
                    point.message());
        }).toList());
    }

    private static Map<String, Object> wave(WaveEquationSpec value) {
        if (value instanceof SchrodingerSpec) return map("type", "schrodinger");
        if (value instanceof RelativisticSchrodingerSpec) return map("type", "relativistic-schrodinger");
        if (value instanceof KleinGordonSpec kg) return map("type", "klein-gordon", "form", kg.form().name(),
                "outerToleranceMeV", kg.outerToleranceMeV(), "maximumOuterIterations", kg.maximumOuterIterations());
        return map("type", "dirac", "kappa", ((DiracSpec) value).kappa());
    }

    private static WaveEquationSpec wave(Map<String, Object> value) {
        return switch (string(value, "type")) {
            case "schrodinger" -> new SchrodingerSpec();
            case "relativistic-schrodinger" -> new RelativisticSchrodingerSpec();
            case "klein-gordon" -> new KleinGordonSpec(enumValue(KleinGordonForm.class,
                    string(value, "form"), "Klein-Gordon form"), number(value, "outerToleranceMeV"),
                    integer(value, "maximumOuterIterations"));
            case "dirac" -> new DiracSpec(integer(value, "kappa"));
            default -> throw new IllegalArgumentException("unsupported wave-equation type '" + value.get("type") + "'");
        };
    }

    private static Map<String, Object> grid(GridSpec value) {
        if (value instanceof LegacyGridSpec g) return map("type", "legacy", "totalPoints", g.totalPoints(),
                "nuclearPoints", g.nuclearPoints(), "atomicScaleFmInverse", g.atomicScale(),
                "nuclearScaleFmInverse", g.nuclearScale(), "regionBoundaryFmInverse", g.regionBoundary(),
                "maximumMomentumScale", g.maximumMomentumScale());
        AdaptiveGridSpec g = (AdaptiveGridSpec) value;
        return map("type", "adaptive", "totalPoints", g.totalPoints(), "nuclearPoints", g.nuclearPoints(),
                "maximumMomentumFmInverse", g.maximumMomentum(), "regionBias", g.regionBias());
    }

    private static GridSpec grid(Map<String, Object> value) {
        return switch (string(value, "type")) {
            case "legacy" -> new LegacyGridSpec(integer(value, "totalPoints"), integer(value, "nuclearPoints"),
                    number(value, "atomicScaleFmInverse"), number(value, "nuclearScaleFmInverse"),
                    number(value, "regionBoundaryFmInverse"), number(value, "maximumMomentumScale"));
            case "adaptive" -> new AdaptiveGridSpec(integer(value, "totalPoints"), integer(value, "nuclearPoints"),
                    number(value, "maximumMomentumFmInverse"), number(value, "regionBias"));
            default -> throw new IllegalArgumentException("unsupported grid type '" + value.get("type") + "'");
        };
    }

    private static Map<String, Object> solver(InverseIterationSpec s) {
        return map("shiftMeV", s.shiftMeV(), "energyToleranceMeV", s.energyTolerance(),
                "residualTolerance", s.residualTolerance(), "minimumIterations", s.minimumIterations(),
                "maximumIterations", s.maximumIterations());
    }

    private static InverseIterationSpec solver(Map<String, Object> value) {
        return new InverseIterationSpec(number(value, "shiftMeV"), number(value, "energyToleranceMeV"),
                number(value, "residualTolerance"), integer(value, "minimumIterations"),
                integer(value, "maximumIterations"));
    }

    private static Map<String, Object> electromagnetic(ElectromagneticSpec value) {
        Map<String, Object> result = charge(value.nuclearCharge());
        result.put("uehlingVacuumPolarization", value.uehlingVacuumPolarization());
        return result;
    }

    private static ElectromagneticSpec electromagnetic(Map<String, Object> value) {
        return new ElectromagneticSpec(charge(value), bool(value, "uehlingVacuumPolarization"));
    }

    private static Map<String, Object> charge(ChargeDistributionSpec value) {
        if (value instanceof PointChargeSpec) return map("type", "point");
        if (value instanceof UniformChargeSpec c) return map("type", "uniform", "rmsRadiusFm", c.rmsRadiusFm());
        if (value instanceof GaussianChargeSpec c) return map("type", "gaussian", "rmsRadiusFm", c.rmsRadiusFm());
        FermiChargeSpec c = (FermiChargeSpec) value;
        return map("type", "fermi", "halfDensityRadiusFm", c.halfDensityRadiusFm(),
                "diffusenessFm", c.diffusenessFm(), "w", c.w());
    }

    private static ChargeDistributionSpec charge(Map<String, Object> value) {
        return switch (string(value, "type")) {
            case "point" -> new PointChargeSpec();
            case "uniform" -> new UniformChargeSpec(number(value, "rmsRadiusFm"));
            case "gaussian" -> new GaussianChargeSpec(number(value, "rmsRadiusFm"));
            case "fermi" -> new FermiChargeSpec(number(value, "halfDensityRadiusFm"),
                    number(value, "diffusenessFm"), number(value, "w"));
            default -> throw new IllegalArgumentException("unsupported charge type '" + value.get("type") + "'");
        };
    }

    private static Map<String, Object> strong(StrongInteractionSpec value) {
        if (value instanceof NoStrongInteractionSpec) return map("type", "none");
        KwonTabakinOpticalPotentialSpec s = (KwonTabakinOpticalPotentialSpec) value;
        Complex length = s.fittedScatteringLengthFm();
        Map<String, Object> result = map("type", "kwon-tabakin-local", "scatteringLengthRealFm",
                length.getReal(), "scatteringLengthImaginaryFm", length.getImaginary());
        result.put("nuclearFormFactor", charge(s.nuclearFormFactor()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static StrongInteractionSpec strong(Map<String, Object> value) {
        return switch (string(value, "type")) {
            case "none" -> new NoStrongInteractionSpec();
            case "kwon-tabakin-local" -> new KwonTabakinOpticalPotentialSpec(
                    new Complex(number(value, "scatteringLengthRealFm"),
                            number(value, "scatteringLengthImaginaryFm")),
                    charge((Map<String, Object>) required(value.get("nuclearFormFactor"), "nuclear form factor")));
            default -> throw new IllegalArgumentException("unsupported strong-interaction type '" + value.get("type") + "'");
        };
    }

    private static FileHeader header(String format) {
        return new FileHeader(format, SCHEMA_VERSION, BopitVersion.VERSION, Instant.now());
    }

    private static void validate(FileHeader header, String format) throws PersistenceException {
        if (header == null) throw new PersistenceException("Missing file header; this is not a versioned BOPIT file.");
        if (!format.equals(header.format())) throw new PersistenceException("Expected format '" + format
                + "' but found '" + header.format() + "'.");
        if (header.schemaVersion() > SCHEMA_VERSION) throw new PersistenceException("Schema version "
                + header.schemaVersion() + " is newer than supported version " + SCHEMA_VERSION
                + "; open this file with a newer BOPIT MDI release.");
        if (header.schemaVersion() < SCHEMA_VERSION) throw new PersistenceException("Schema version "
                + header.schemaVersion() + " has no migration to supported version " + SCHEMA_VERSION + ".");
    }

    private static <T> T readDocument(Path path, Class<T> type, String format)
            throws IOException, PersistenceException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) throw new PersistenceException("BOPIT JSON root must be an object.");
            JsonObject object = root.getAsJsonObject();
            FileHeader header = GSON.fromJson(object.get("header"), FileHeader.class);
            validate(header, format);
            return GSON.fromJson(root, type);
        } catch (PersistenceException error) {
            throw error;
        } catch (RuntimeException error) {
            throw new PersistenceException("Malformed JSON in " + path.getFileName() + ": " + message(error), error);
        }
    }

    private static void write(Object value, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) { GSON.toJson(value, writer); }
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        return result;
    }
    private static String string(Map<String, Object> map, String key) {
        return required(map.get(key), key).toString();
    }
    private static double number(Map<String, Object> map, String key) {
        Object value = required(map.get(key), key);
        if (!(value instanceof Number number)) throw new IllegalArgumentException(key + " must be numeric");
        return number.doubleValue();
    }
    private static int integer(Map<String, Object> map, String key) {
        double value = number(map, key);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        return (int) value;
    }
    private static boolean bool(Map<String, Object> map, String key) {
        Object value = required(map.get(key), key);
        if (!(value instanceof Boolean result)) throw new IllegalArgumentException(key + " must be boolean");
        return result;
    }
    private static <T> T required(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " is missing");
        return value;
    }
    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String label) {
        try { return Enum.valueOf(type, required(value, label)); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("unsupported " + label + " '" + value + "'"); }
    }
    private static String message(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
