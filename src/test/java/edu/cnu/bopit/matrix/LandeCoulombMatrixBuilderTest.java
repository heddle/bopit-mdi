package edu.cnu.bopit.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.physics.coulomb.CoulombKernel;
import edu.cnu.bopit.physics.coulomb.PointCoulombKernel;

class LandeCoulombMatrixBuilderTest {
    @Test
    void buildsFiniteSymmetricMatrixWithoutDiagonalKernelCalls() {
        MomentumGrid grid = new LegacyMappedGridFactory().create(
                new LegacyGridSpec(20, 6, 0.095, 0.7, 0.3, 1_000.0));
        PointCoulombKernel delegate = new PointCoulombKernel(16, 7.29735e-3, 197.32858);
        AtomicInteger calls = new AtomicInteger();
        CoulombKernel spy = (l, p, pp) -> {
            assertTrue(p != pp);
            calls.incrementAndGet();
            return delegate.offDiagonal(l, p, pp);
        };
        CoulombMatrix result = new LandeCoulombMatrixBuilder().build(
                2, grid, spy, 16, 7.29735e-3, 197.32858);
        assertEquals(grid.size() * (grid.size() - 1) / 2, calls.get());
        RealMatrix matrix = result.operatorMeV();
        for (int i = 0; i < grid.size(); i++) {
            assertTrue(Double.isFinite(matrix.getEntry(i, i)));
        }
        assertEquals(0.0, result.diagnostics().symmetryResidual(), 1e-14);
    }
}
