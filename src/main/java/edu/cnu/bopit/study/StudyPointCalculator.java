package edu.cnu.bopit.study;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.calculation.ComplexKleinGordonCalculator;
import edu.cnu.bopit.calculation.DiracCalculator;
import edu.cnu.bopit.calculation.KleinGordonCalculator;
import edu.cnu.bopit.calculation.PointCoulombCalculator;
import edu.cnu.bopit.calculation.StrongInteractionCalculator;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.KleinGordonSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;

/** Executes one complete problem and extracts common numeric observables. */
public final class StudyPointCalculator {
    public StudyPointOutcome calculate(BopitProblem problem, PhysicalConstantSet constants,
            CalculationMonitor monitor) {
        if (problem.waveEquation() instanceof DiracSpec) {
            var result = new DiracCalculator().calculate(problem, constants, monitor);
            return outcome(result.solverResult().converged(), result.bindingEnergyMeV(),
                    result.exactPointCoulombEnergyMeV(), result.absoluteErrorMeV(),
                    Double.NaN, Double.NaN, result.solverResult().history().size(),
                    lastResidual(result.solverResult().history()));
        }
        if (problem.waveEquation() instanceof KleinGordonSpec) {
            if (problem.strongInteraction() instanceof NoStrongInteractionSpec) {
                var result = new KleinGordonCalculator().calculate(problem, constants, monitor);
                return outcome(result.converged(), result.bindingEnergyMeV(),
                        result.exactPointCoulombEnergyMeV(),
                        Math.abs(result.bindingEnergyMeV() - result.exactPointCoulombEnergyMeV()),
                        Double.NaN, Double.NaN, totalInnerIterations(result.outerHistory()),
                        result.outerHistory().get(result.outerHistory().size() - 1).innerResidualMeV2());
            }
            var result = new ComplexKleinGordonCalculator().calculate(problem, constants, monitor);
            return outcome(result.converged(), result.bindingEnergyMeV().getReal(),
                    result.electromagneticReferenceEnergyMeV(),
                    Math.abs(result.strongInteractionShiftMeV()), result.strongInteractionShiftMeV(),
                    result.widthMeV(), result.finalInnerResult().history().size(),
                    result.finalInnerResult().history().get(
                            result.finalInnerResult().history().size() - 1).residualNorm());
        }
        if (!(problem.strongInteraction() instanceof NoStrongInteractionSpec)) {
            var ordinary = problem.solver();
            var spec = new ComplexInverseIterationSpec(new Complex(ordinary.shiftMeV(), -0.001),
                    ordinary.energyTolerance(), ordinary.residualTolerance(),
                    ordinary.minimumIterations(), ordinary.maximumIterations());
            var result = new StrongInteractionCalculator().calculate(problem, constants, spec, monitor);
            return outcome(result.solverResult().converged(),
                    result.complexBindingEnergyMeV().getReal(),
                    result.electromagneticReferenceEnergyMeV(),
                    Math.abs(result.strongInteractionShiftMeV()), result.strongInteractionShiftMeV(),
                    result.widthMeV(), result.solverResult().history().size(),
                    result.solverResult().history().get(
                            result.solverResult().history().size() - 1).residualNorm());
        }
        var result = new PointCoulombCalculator().calculate(problem, constants, monitor);
        return outcome(result.solverResult().converged(), result.calculatedEnergyMeV(),
                result.referenceEnergyMeV(), result.absoluteErrorMeV(), Double.NaN, Double.NaN,
                result.solverResult().history().size(), lastResidual(result.solverResult().history()));
    }

    private static StudyPointOutcome outcome(boolean converged, double energy, double reference,
            double error, double shift, double width, int iterations, double residual) {
        Map<StudyObservable, Double> values = new EnumMap<>(StudyObservable.class);
        values.put(StudyObservable.BINDING_ENERGY_MEV, energy);
        values.put(StudyObservable.REFERENCE_ENERGY_MEV, reference);
        values.put(StudyObservable.ABSOLUTE_ERROR_MEV, error);
        values.put(StudyObservable.STRONG_SHIFT_MEV, shift);
        values.put(StudyObservable.WIDTH_MEV, width);
        values.put(StudyObservable.ITERATIONS, (double) iterations);
        values.put(StudyObservable.RESIDUAL, residual);
        return new StudyPointOutcome(converged, values,
                converged ? "converged" : "solver did not converge");
    }

    private static double lastResidual(java.util.List<edu.cnu.bopit.solver.IterationRecord> history) {
        return history.get(history.size() - 1).residualNorm();
    }
    private static int totalInnerIterations(
            java.util.List<edu.cnu.bopit.calculation.KleinGordonOuterRecord> history) {
        return history.stream().mapToInt(edu.cnu.bopit.calculation.KleinGordonOuterRecord::innerIterations).sum();
    }

    public record StudyPointOutcome(boolean converged,
            Map<StudyObservable, Double> observables, String message) {
        public StudyPointOutcome {
            observables = Map.copyOf(observables);
        }
    }
}
