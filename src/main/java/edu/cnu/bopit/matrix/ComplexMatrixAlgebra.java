package edu.cnu.bopit.matrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/** Basic immutable algebra for the split real/imaginary complex representation. */
public final class ComplexMatrixAlgebra {
    private ComplexMatrixAlgebra() { }

    public static ComplexMatrix zero(int dimension) {
        return new ComplexMatrix(new Array2DRowRealMatrix(dimension, dimension),
                new Array2DRowRealMatrix(dimension, dimension));
    }

    public static ComplexMatrix real(RealMatrix matrix) {
        return new ComplexMatrix(matrix,
                new Array2DRowRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension()));
    }

    public static ComplexMatrix add(ComplexMatrix left, ComplexMatrix right) {
        requireEqual(left, right);
        return new ComplexMatrix(left.real().add(right.real()),
                left.imaginary().add(right.imaginary()));
    }

    public static ComplexMatrix subtract(ComplexMatrix left, ComplexMatrix right) {
        requireEqual(left, right);
        return new ComplexMatrix(left.real().subtract(right.real()),
                left.imaginary().subtract(right.imaginary()));
    }

    public static ComplexMatrix multiply(ComplexMatrix left, ComplexMatrix right) {
        if (left.columns() != right.rows()) throw new IllegalArgumentException("incompatible dimensions");
        RealMatrix a = left.real();
        RealMatrix b = left.imaginary();
        RealMatrix c = right.real();
        RealMatrix d = right.imaginary();
        return new ComplexMatrix(a.multiply(c).subtract(b.multiply(d)),
                a.multiply(d).add(b.multiply(c)));
    }

    public static ComplexMatrix scale(ComplexMatrix matrix, Complex scalar) {
        RealMatrix real = matrix.real();
        RealMatrix imaginary = matrix.imaginary();
        return new ComplexMatrix(real.scalarMultiply(scalar.getReal())
                        .subtract(imaginary.scalarMultiply(scalar.getImaginary())),
                real.scalarMultiply(scalar.getImaginary())
                        .add(imaginary.scalarMultiply(scalar.getReal())));
    }

    public static ComplexMatrix addDiagonal(ComplexMatrix matrix, double[] realDiagonal) {
        if (realDiagonal.length != matrix.rows() || matrix.rows() != matrix.columns()) {
            throw new IllegalArgumentException("diagonal must match square matrix");
        }
        RealMatrix real = matrix.real();
        for (int i = 0; i < realDiagonal.length; i++) real.addToEntry(i, i, realDiagonal[i]);
        return new ComplexMatrix(real, matrix.imaginary());
    }

    private static void requireEqual(ComplexMatrix left, ComplexMatrix right) {
        if (left.rows() != right.rows() || left.columns() != right.columns()) {
            throw new IllegalArgumentException("matrix dimensions must match");
        }
    }
}
