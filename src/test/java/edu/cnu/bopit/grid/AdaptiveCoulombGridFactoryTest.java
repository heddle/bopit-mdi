package edu.cnu.bopit.grid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.AdaptiveGridSpec;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;

class AdaptiveCoulombGridFactoryTest {
    private static final AtomicSystem SULFUR = new AtomicSystem(16, 32,
            OrbitingParticle.KAON_MINUS, PublishedConstantSets.BOPIT_1990.kaonMassMeV(),
            PublishedConstantSets.BOPIT_1990.nuclearMassMeV(16, 32));

    @Test
    void reproducesBopit1990AutomaticGridPoints() {
        MomentumGrid grid = new AdaptiveCoulombGridFactory().create(
                new AdaptiveGridSpec(100, 20, 100_000.0, 0.5), SULFUR,
                new QuantumState(3, 2), PublishedConstantSets.BOPIT_1990);
        assertEquals(100, grid.size());
        // The source uses single precision for mapped points.
        assertEquals(2.617980e-5, grid.point(0), 2e-9);
        assertEquals(3.383351e-4, grid.point(2), 2e-9);
        assertEquals(2.984256e-1, grid.point(79), 2e-7);
        assertEquals(3.054128e-1, grid.point(80), 2e-7);
        assertEquals(2.253234e2, grid.point(99), 2e-2);

        AdaptiveGridDiagnostics diagnostics = assertInstanceOf(
                AdaptiveGridDiagnostics.class, grid.diagnostics());
        assertEquals(0, diagnostics.atomicNodes().length);
        assertArrayEquals(new int[] {80, 20}, diagnostics.pointsPerRegion());
        assertTrue(diagnostics.warnings().isEmpty());
    }

    @Test
    void exposesNodesAndPointCountAdjustmentForMultipleLobes() {
        MomentumGrid grid = new AdaptiveCoulombGridFactory().create(
                new AdaptiveGridSpec(50, 10, 1_000.0, 0.5), SULFUR,
                new QuantumState(3, 0), PublishedConstantSets.BOPIT_1990);
        AdaptiveGridDiagnostics diagnostics = (AdaptiveGridDiagnostics) grid.diagnostics();
        assertEquals(2, diagnostics.atomicNodes().length);
        assertArrayEquals(new int[] {12, 12, 12, 10}, diagnostics.pointsPerRegion());
        assertEquals(46, diagnostics.actualTotalPoints());
        assertEquals(46, grid.size());
        assertEquals(1, diagnostics.warnings().stream()
                .filter(message -> message.contains("point count adjusted")).count());
    }
}
