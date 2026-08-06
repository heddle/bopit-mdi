package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/** Converts A+iB to the real block matrix [A -B; B A]. */
public final class ComplexBlockMatrices {
    private ComplexBlockMatrices() { }

    public static RealMatrix toRealBlock(ComplexMatrix matrix) {
        if (matrix.rows() != matrix.columns()) throw new IllegalArgumentException("matrix must be square");
        int n = matrix.rows();
        RealMatrix real = matrix.real();
        RealMatrix imaginary = matrix.imaginary();
        RealMatrix block = new Array2DRowRealMatrix(2 * n, 2 * n);
        block.setSubMatrix(real.getData(), 0, 0);
        block.setSubMatrix(imaginary.scalarMultiply(-1.0).getData(), 0, n);
        block.setSubMatrix(imaginary.getData(), n, 0);
        block.setSubMatrix(real.getData(), n, n);
        return block;
    }

    /** Real block representation of H-shift*I for a complex shift. */
    public static RealMatrix shiftedRealBlock(ComplexMatrix matrix,
            org.apache.commons.math3.complex.Complex shift) {
        RealMatrix real = matrix.real();
        RealMatrix imaginary = matrix.imaginary();
        for (int i = 0; i < matrix.rows(); i++) {
            real.addToEntry(i, i, -shift.getReal());
            imaginary.addToEntry(i, i, -shift.getImaginary());
        }
        return toRealBlock(new ComplexMatrix(real, imaginary));
    }
}
