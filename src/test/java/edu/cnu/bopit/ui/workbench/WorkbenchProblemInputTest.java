package edu.cnu.bopit.ui.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.BopitProblem;

class WorkbenchProblemInputTest {
    @Test
    void validAdaptiveInputBuildsImmutableProblem() {
        WorkbenchProblemInput input = sulfurAdaptiveInput();
        assertTrue(input.validate().isValid());
        BopitProblem problem = input.toProblem();
        assertEquals(16, problem.atomicSystem().nuclearCharge());
        assertEquals(3, problem.quantumState().principalN());
        assertInstanceOf(AdaptiveGridSpec.class, problem.grid());
    }

    @Test
    void reportsCrossFieldErrorsTogether() {
        WorkbenchProblemInput input = new WorkbenchProblemInput(
                16, 10, 493.667, 30032.0, 2, 2,
                GridKind.ADAPTIVE, 9, 3,
                0.095, 0.7, 0.3, 1000.0,
                -1.0, 1.0, -0.37, 1e-12, 1e-10, 5, 4);
        ValidationReport report = input.validate();
        assertFalse(report.isValid());
        assertTrue(report.errors().size() >= 5);
    }

    private static WorkbenchProblemInput sulfurAdaptiveInput() {
        return new WorkbenchProblemInput(
                16, 32, 493.667, 30032.0, 3, 2,
                GridKind.ADAPTIVE, 100, 10,
                0.095, 0.7, 0.3, 1000.0,
                100000.0, 0.5, -0.37, 1e-12, 1e-10, 5, 50);
    }
}
