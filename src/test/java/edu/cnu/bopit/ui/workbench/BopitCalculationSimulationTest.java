package edu.cnu.bopit.ui.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.mdi.sim.SimulationEngine;
import edu.cnu.mdi.sim.SimulationEngineConfig;
import edu.cnu.mdi.sim.SimulationContext;
import edu.cnu.mdi.sim.SimulationListener;

class BopitCalculationSimulationTest {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void engineRunsCalculationAndRetainsResult() throws Exception {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var simulation = new BopitCalculationSimulation(problem, constants);
        var engine = new SimulationEngine(simulation,
                new SimulationEngineConfig(0, 0, 0, true));
        simulation.bindEngine(engine);
        CountDownLatch terminated = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        engine.addListener(new SimulationListener() {
            @Override
            public void onDone(SimulationContext context) {
                terminated.countDown();
            }

            @Override
            public void onFail(SimulationContext context, Throwable error) {
                failure.set(error);
                terminated.countDown();
            }
        });

        engine.start();
        assertTrue(terminated.await(10, TimeUnit.SECONDS), "background calculation timed out");
        SwingUtilities.invokeAndWait(() -> { });
        assertNull(failure.get());

        var result = simulation.result().orElseThrow();
        assertEquals(-0.367806279690, result.calculatedEnergyMeV(), 2e-12);
    }
}
