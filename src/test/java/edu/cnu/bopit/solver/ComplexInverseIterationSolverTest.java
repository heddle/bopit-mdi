package edu.cnu.bopit.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.CalculationMonitor;
import edu.cnu.bopit.matrix.ComplexMatrix;
import edu.cnu.bopit.model.ComplexInverseIterationSpec;

class ComplexInverseIterationSolverTest {
    @Test
    void selectsComplexEigenpairNearComplexShift() {
        var matrix = new ComplexMatrix(
                new Array2DRowRealMatrix(new double[][] {{-2.0, 0.0}, {0.0, 4.0}}),
                new Array2DRowRealMatrix(new double[][] {{-0.25, 0.0}, {0.0, -1.0}}));
        var spec = new ComplexInverseIterationSpec(new Complex(-1.8, -0.2),
                1e-13, 1e-12, 4, 40);
        var result = new ComplexInverseIterationSolver().solve(matrix, spec, CalculationMonitor.NONE);
        assertTrue(result.converged());
        assertEquals(-2.0, result.eigenvalueMeV().getReal(), 1e-12);
        assertEquals(-0.25, result.eigenvalueMeV().getImaginary(), 1e-12);
        assertTrue(result.history().get(result.history().size() - 1).residualNorm() < 1e-12);
    }
}
