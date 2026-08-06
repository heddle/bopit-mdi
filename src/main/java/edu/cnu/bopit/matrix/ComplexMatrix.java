package edu.cnu.bopit.matrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.RealMatrix;

/** Immutable complex matrix represented by separate real and imaginary parts. */
public record ComplexMatrix(RealMatrix real, RealMatrix imaginary) {
    public ComplexMatrix {
        if (real == null || imaginary == null
                || real.getRowDimension() != imaginary.getRowDimension()
                || real.getColumnDimension() != imaginary.getColumnDimension()) {
            throw new IllegalArgumentException("real and imaginary matrices must have equal dimensions");
        }
        real = real.copy();
        imaginary = imaginary.copy();
    }

    @Override public RealMatrix real() { return real.copy(); }
    @Override public RealMatrix imaginary() { return imaginary.copy(); }
    public int rows() { return real.getRowDimension(); }
    public int columns() { return real.getColumnDimension(); }
    public Complex entry(int row, int column) {
        return new Complex(real.getEntry(row, column), imaginary.getEntry(row, column));
    }
}
