package edu.cnu.bopit.matrix;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.jupiter.api.Test;

class ComplexBlockMatricesTest {
    @Test
    void blockMatrixPerformsComplexMultiplication() {
        var matrix = new ComplexMatrix(
                new Array2DRowRealMatrix(new double[][] {{2, 3}, {5, 7}}),
                new Array2DRowRealMatrix(new double[][] {{11, 13}, {17, 19}}));
        var block = ComplexBlockMatrices.toRealBlock(matrix);
        var product = block.operate(new ArrayRealVector(new double[] {23, 29, 31, 37}));
        assertArrayEquals(new double[] {-689, -912, 803, 1356}, product.toArray(), 1e-12);
        assertEquals(4, block.getRowDimension());
    }
}
