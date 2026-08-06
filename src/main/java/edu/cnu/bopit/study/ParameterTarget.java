package edu.cnu.bopit.study;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ChargeDistributionSpec;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.GaussianChargeSpec;
import edu.cnu.bopit.model.KleinGordonForm;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.model.UniformChargeSpec;
import edu.cnu.bopit.model.WaveEquationSpec;

/** Supported controlled mutations of an immutable base problem. */
public enum ParameterTarget {
    TOTAL_GRID_POINTS,
    MAXIMUM_MOMENTUM_FM_INVERSE,
    ADAPTIVE_REGION_BIAS,
    GRID_KIND,
    STRONG_REAL_SCATTERING_LENGTH_FM,
    STRONG_IMAGINARY_SCATTERING_LENGTH_FM,
    CHARGE_RADIUS_FM,
    WAVE_EQUATION;

    public BopitProblem apply(BopitProblem problem, double value) {
        return switch (this) {
            case TOTAL_GRID_POINTS -> withGrid(problem, totalPoints(problem, value));
            case MAXIMUM_MOMENTUM_FM_INVERSE -> withGrid(problem, maximumMomentum(problem, value));
            case ADAPTIVE_REGION_BIAS -> withGrid(problem, regionBias(problem, value));
            case GRID_KIND -> withGrid(problem, gridKind(problem, value));
            case STRONG_REAL_SCATTERING_LENGTH_FM -> scattering(problem, value, true);
            case STRONG_IMAGINARY_SCATTERING_LENGTH_FM -> scattering(problem, value, false);
            case CHARGE_RADIUS_FM -> chargeRadius(problem, value);
            case WAVE_EQUATION -> waveEquation(problem, value);
        };
    }

    private static edu.cnu.bopit.model.GridSpec totalPoints(BopitProblem problem, double value) {
        int total = requireInteger(value, "total grid points");
        if (problem.grid() instanceof LegacyGridSpec grid) {
            return new LegacyGridSpec(total, grid.nuclearPoints(), grid.atomicScale(),
                    grid.nuclearScale(), grid.regionBoundary(), grid.maximumMomentumScale());
        }
        AdaptiveGridSpec grid = (AdaptiveGridSpec) problem.grid();
        return new AdaptiveGridSpec(total, grid.nuclearPoints(), grid.maximumMomentum(), grid.regionBias());
    }

    private static edu.cnu.bopit.model.GridSpec maximumMomentum(BopitProblem problem, double value) {
        requirePositive(value, "maximum momentum");
        if (problem.grid() instanceof LegacyGridSpec grid) {
            return new LegacyGridSpec(grid.totalPoints(), grid.nuclearPoints(), grid.atomicScale(),
                    grid.nuclearScale(), grid.regionBoundary(), value);
        }
        AdaptiveGridSpec grid = (AdaptiveGridSpec) problem.grid();
        return new AdaptiveGridSpec(grid.totalPoints(), grid.nuclearPoints(), value, grid.regionBias());
    }

    private static edu.cnu.bopit.model.GridSpec regionBias(BopitProblem problem, double value) {
        if (!(problem.grid() instanceof AdaptiveGridSpec grid)) {
            throw new IllegalArgumentException("region bias requires an adaptive grid");
        }
        return new AdaptiveGridSpec(grid.totalPoints(), grid.nuclearPoints(),
                grid.maximumMomentum(), value);
    }

    private static edu.cnu.bopit.model.GridSpec gridKind(BopitProblem problem, double value) {
        int code = requireInteger(value, "grid kind");
        if (code == 0) {
            if (problem.grid() instanceof LegacyGridSpec legacy) return legacy;
            AdaptiveGridSpec adaptive = (AdaptiveGridSpec) problem.grid();
            int total = even(adaptive.totalPoints());
            int nuclear = even(Math.min(adaptive.nuclearPoints(), total - 2));
            return new LegacyGridSpec(total, nuclear, 0.095, 0.7, 0.3,
                    adaptive.maximumMomentum());
        }
        if (code == 1) {
            if (problem.grid() instanceof AdaptiveGridSpec adaptive) return adaptive;
            LegacyGridSpec legacy = (LegacyGridSpec) problem.grid();
            return new AdaptiveGridSpec(legacy.totalPoints(), legacy.nuclearPoints(),
                    legacy.maximumMomentumScale(), 0.5);
        }
        throw new IllegalArgumentException("grid kind code must be 0 or 1");
    }

    private static BopitProblem scattering(BopitProblem problem, double value, boolean realPart) {
        if (!(problem.strongInteraction() instanceof KwonTabakinOpticalPotentialSpec optical)) {
            throw new IllegalArgumentException("scattering-length study requires the Kwon-Tabakin model");
        }
        Complex old = optical.fittedScatteringLengthFm();
        Complex replacement = realPart ? new Complex(value, old.getImaginary())
                : new Complex(old.getReal(), value);
        return copy(problem, problem.waveEquation(), problem.grid(), problem.electromagnetic(),
                new KwonTabakinOpticalPotentialSpec(replacement, optical.nuclearFormFactor()));
    }

    private static BopitProblem chargeRadius(BopitProblem problem, double value) {
        requirePositive(value, "charge radius");
        ChargeDistributionSpec old = problem.electromagnetic().nuclearCharge();
        ChargeDistributionSpec replacement;
        if (old instanceof UniformChargeSpec) replacement = new UniformChargeSpec(value);
        else if (old instanceof GaussianChargeSpec) replacement = new GaussianChargeSpec(value);
        else if (old instanceof FermiChargeSpec fermi) {
            replacement = new FermiChargeSpec(value, fermi.diffusenessFm(), fermi.w());
        } else throw new IllegalArgumentException("charge-radius study requires a finite charge model");
        return copy(problem, problem.waveEquation(), problem.grid(),
                new ElectromagneticSpec(replacement,
                        problem.electromagnetic().uehlingVacuumPolarization()),
                problem.strongInteraction());
    }

    private static BopitProblem waveEquation(BopitProblem problem, double value) {
        int code = requireInteger(value, "wave equation");
        WaveEquationSpec equation = switch (code) {
            case 0 -> new SchrodingerSpec();
            case 1 -> new RelativisticSchrodingerSpec();
            case 2 -> kg(KleinGordonForm.ENERGY_WEIGHTED_NUCLEAR);
            case 3 -> kg(KleinGordonForm.FULL_VECTOR);
            case 4 -> kg(KleinGordonForm.MASS_WEIGHTED_NUCLEAR);
            case 5 -> kg(KleinGordonForm.SCALAR_NUCLEAR);
            case 6 -> new DiracSpec(-(problem.quantumState().orbitalL() + 1));
            default -> throw new IllegalArgumentException("wave-equation code must be 0 through 6");
        };
        return copy(problem, equation, problem.grid(), problem.electromagnetic(),
                problem.strongInteraction());
    }

    private static KleinGordonSpec kg(KleinGordonForm form) {
        return new KleinGordonSpec(form, 1e-9, 15);
    }

    private static BopitProblem withGrid(BopitProblem problem, edu.cnu.bopit.model.GridSpec grid) {
        return copy(problem, problem.waveEquation(), grid, problem.electromagnetic(),
                problem.strongInteraction());
    }

    private static BopitProblem copy(BopitProblem problem, WaveEquationSpec equation,
            edu.cnu.bopit.model.GridSpec grid, ElectromagneticSpec electromagnetic,
            edu.cnu.bopit.model.StrongInteractionSpec strong) {
        return new BopitProblem(problem.atomicSystem(), problem.quantumState(), equation,
                grid, problem.solver(), electromagnetic, strong);
    }

    private static int requireInteger(double value, String name) {
        if (!Double.isFinite(value) || Math.rint(value) != value) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        return (int) value;
    }
    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) throw new IllegalArgumentException(name + " must be positive");
    }
    private static int even(int value) { return (value & 1) == 0 ? value : value - 1; }
}
