package edu.cnu.bopit.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.model.InverseIterationSpec;

class InverseIterationSolverTest {
    @Test
    void findsSelectedKnownEigenpair() {
        var matrix = new Array2DRowRealMatrix(new double[][] {{2.0, 0.5}, {0.5, 1.0}});
        var spec = new InverseIterationSpec(0.75, 1e-13, 1e-12, 2, 30);
        InverseIterationResult result = new InverseIterationSolver().solve(matrix, spec, CalculationMonitor.NONE);
        assertTrue(result.converged());
        assertEquals((3.0 - Math.sqrt(2.0)) / 2.0, result.eigenvalueMeV(), 1e-12);
        assertTrue(result.history().get(result.history().size() - 1).residualNorm() < 1e-12);
    }
}
