package edu.cnu.bopit.ui.workbench;

import java.util.ArrayList;
import java.util.List;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.GridSpec;
import edu.cnu.bopit.model.InverseIterationSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.model.SchrodingerSpec;

/**
 * Editable Stage 3 values before conversion to the validated numerical model.
 * Dimensional values use MeV or fm^-1 as indicated by their names.
 */
public record WorkbenchProblemInput(
        int nuclearCharge,
        int massNumber,
        double particleMassMeV,
        double nuclearMassMeV,
        int principalN,
        int orbitalL,
        GridKind gridKind,
        int totalPoints,
        int nuclearPoints,
        double legacyAtomicScaleFmInverse,
        double legacyNuclearScaleFmInverse,
        double legacyBoundaryFmInverse,
        double legacyMaximumMomentumScale,
        double adaptiveMaximumMomentumFmInverse,
        double adaptiveRegionBias,
        double shiftMeV,
        double energyToleranceMeV,
        double residualTolerance,
        int minimumIterations,
        int maximumIterations) {

    /** Validate all fields without requiring Swing components. */
    public ValidationReport validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (nuclearCharge <= 0) errors.add("Nuclear charge Z must be positive.");
        if (massNumber < nuclearCharge) errors.add("Mass number A must be at least Z.");
        requirePositive(particleMassMeV, "Particle mass", errors);
        requirePositive(nuclearMassMeV, "Nuclear mass", errors);
        if (principalN < 1) errors.add("Principal quantum number n must be at least 1.");
        if (orbitalL < 0 || orbitalL >= principalN) errors.add("Orbital l must satisfy 0 <= l < n.");
        if (gridKind == null) errors.add("A momentum-grid type is required.");
        if (totalPoints < 4) errors.add("Total grid points must be at least 4.");
        if (nuclearPoints <= 0 || nuclearPoints >= totalPoints) {
            errors.add("Nuclear grid points must be positive and smaller than the total.");
        }
        if ((nuclearPoints & 1) != 0) errors.add("Nuclear grid points must be even.");
        if (gridKind == GridKind.LEGACY) {
            if (((totalPoints - nuclearPoints) & 1) != 0) {
                errors.add("Legacy atomic-region grid points must be even.");
            }
            requirePositive(legacyAtomicScaleFmInverse, "Legacy atomic scale", errors);
            requirePositive(legacyNuclearScaleFmInverse, "Legacy nuclear scale", errors);
            requirePositive(legacyBoundaryFmInverse, "Legacy region boundary", errors);
            requirePositive(legacyMaximumMomentumScale, "Legacy maximum-momentum scale", errors);
        } else if (gridKind == GridKind.ADAPTIVE) {
            requirePositive(adaptiveMaximumMomentumFmInverse, "Adaptive maximum momentum", errors);
            if (!Double.isFinite(adaptiveRegionBias)
                    || adaptiveRegionBias <= 0.0 || adaptiveRegionBias >= 1.0) {
                errors.add("Adaptive region bias must lie strictly between 0 and 1.");
            }
        }
        if (!Double.isFinite(shiftMeV)) errors.add("Inverse-iteration shift must be finite.");
        requirePositive(energyToleranceMeV, "Energy tolerance", errors);
        requirePositive(residualTolerance, "Residual tolerance", errors);
        if (minimumIterations < 1) errors.add("Minimum iterations must be positive.");
        if (maximumIterations < minimumIterations) {
            errors.add("Maximum iterations must be at least the minimum.");
        }
        if (totalPoints < 20) warnings.add("Very small grids may not resolve the Coulomb state reliably.");
        return new ValidationReport(errors, warnings);
    }

    /** Convert valid editor values into the immutable headless problem model. */
    public BopitProblem toProblem() {
        ValidationReport report = validate();
        if (!report.isValid()) {
            throw new IllegalArgumentException(String.join(" ", report.errors()));
        }
        AtomicSystem atom = new AtomicSystem(nuclearCharge, massNumber,
                OrbitingParticle.KAON_MINUS, particleMassMeV, nuclearMassMeV);
        GridSpec grid = gridKind == GridKind.LEGACY
                ? new LegacyGridSpec(totalPoints, nuclearPoints,
                        legacyAtomicScaleFmInverse, legacyNuclearScaleFmInverse,
                        legacyBoundaryFmInverse, legacyMaximumMomentumScale)
                : new AdaptiveGridSpec(totalPoints, nuclearPoints,
                        adaptiveMaximumMomentumFmInverse, adaptiveRegionBias);
        return new BopitProblem(atom, new QuantumState(principalN, orbitalL),
                new SchrodingerSpec(), grid,
                new InverseIterationSpec(shiftMeV, energyToleranceMeV,
                        residualTolerance, minimumIterations, maximumIterations));
    }

    private static void requirePositive(double value, String label, List<String> errors) {
        if (!Double.isFinite(value) || value <= 0.0) {
            errors.add(label + " must be finite and positive.");
        }
    }
}
