package edu.cnu.bopit.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.model.LegacyGridSpec;

class LegacyMappedGridFactoryTest {
    private static final LegacyGridSpec SULFUR =
            new LegacyGridSpec(40, 10, 0.095, 0.7, 0.3, 1_000.0);

    @Test
    void reproducesPublishedLeadingMomentumPoints() {
        MomentumGrid grid = new LegacyMappedGridFactory().create(SULFUR);
        assertEquals(40, grid.size());
        // Published points came from predominantly single-precision mapping code.
        assertEquals(2.316088e-4, grid.point(0), 2e-9);
        assertEquals(1.213702e-3, grid.point(1), 3e-9);
        assertEquals(0.1217167, grid.point(19), 2e-7);
        assertTrue(grid.point(30) > 0.3);
        assertTrue(grid.point(39) < 1_000.3);
    }

    @Test
    void rejectsOddRegionCountsInsteadOfMutatingThem() {
        assertThrows(IllegalArgumentException.class,
                () -> new LegacyGridSpec(40, 9, 0.095, 0.7, 0.3, 1_000.0));
    }
}
