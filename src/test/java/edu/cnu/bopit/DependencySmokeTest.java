package edu.cnu.bopit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.jupiter.api.Test;

import edu.cnu.mdi.sim.ProgressInfo;

class DependencySmokeTest {
    @Test
    void resolvesPinnedDependencies() {
        assertEquals(2.0, new ArrayRealVector(new double[] { 2.0 }).getEntry(0));
        assertNotNull(ProgressInfo.determinate(0.5, "testing"));
    }
}
