package edu.cnu.bopit.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.grid.LegacyMappedGridFactory;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.physics.coulomb.LegendreQ;
import edu.cnu.bopit.physics.electromagnetic.PointChargeDistribution;
import edu.cnu.bopit.physics.electromagnetic.UniformChargeDistribution;

class ElectromagneticMatrixBuilderTest {
    @Test
    void finiteSizeCorrectionIsFiniteSymmetricAndRepulsive() {
        var grid = new LegacyMappedGridFactory().create(
                new LegacyGridSpec(20, 6, 0.095, 0.7, 0.3, 1_000.0));
        var constants = PublishedConstantSets.BOPIT_1990;
        var matrix = new FiniteSizeCoulombMatrixBuilder().buildCorrection(2, grid,
                new UniformChargeDistribution(2.56), 16,
                constants.fineStructureConstant(), constants.hbarCMeVFm());
        assertEquals(0.0, matrix.subtract(matrix.transpose()).getNorm(), 1e-14);
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            assertTrue(Double.isFinite(matrix.getEntry(i, i)));
        }
        assertTrue(matrix.getTrace() > 0.0);
    }

    @Test
    void pointUehlingOperatorIsFiniteSymmetricAndAttractive() {
        var grid = new LegacyMappedGridFactory().create(
                new LegacyGridSpec(12, 4, 0.095, 0.7, 0.3, 1_000.0));
        var matrix = new UehlingMatrixBuilder().build(2, grid,
                new PointChargeDistribution(), 16, PublishedConstantSets.BOPIT_1990);
        assertEquals(0.0, matrix.subtract(matrix.transpose()).getNorm(), 1e-14);
        assertTrue(matrix.getTrace() < 0.0);
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            assertTrue(Double.isFinite(matrix.getEntry(i, i)));
        }

        int i = 2;
        int j = 8;
        double expectedRaw = independentPointProjection(2, grid.point(i), grid.point(j), 16);
        double actualRaw = matrix.getEntry(i, j) / (grid.point(i) * Math.sqrt(grid.weight(i))
                * grid.point(j) * Math.sqrt(grid.weight(j)));
        assertEquals(expectedRaw, actualRaw, Math.abs(expectedRaw) * 4e-6);
    }

    private static double independentPointProjection(int l, double p, double pp, int zNucleus) {
        var constants = PublishedConstantSets.BOPIT_1990;
        var rule = new edu.cnu.bopit.grid.GaussLegendreRules().create(160);
        double integral = 0.0;
        double lambda = constants.hbarCMeVFm() / constants.electronMassMeV();
        double z = (p * p + pp * pp) / (2.0 * p * pp);
        for (int k = 0; k < rule.size(); k++) {
            double s = 0.5 * (rule.node(k) + 1.0);
            double t = 1.0 + s / (1.0 - s);
            double jacobian = 0.5 / ((1.0 - s) * (1.0 - s));
            double shiftedZ = z + Math.pow(2.0 * t / lambda, 2) / (2.0 * p * pp);
            integral += rule.weight(k) * jacobian
                    * UehlingMatrixBuilder.uehlingWeight(t, constants.fineStructureConstant())
                    * LegendreQ.value(l, shiftedZ);
        }
        return -zNucleus * constants.fineStructureConstant() * constants.hbarCMeVFm()
                * integral / (Math.PI * p * pp);
    }
}
