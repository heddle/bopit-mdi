package edu.cnu.bopit.ui.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.mdi.sim.CompletionStatus;
import edu.cnu.mdi.sim.task.BackgroundTasks;
import edu.cnu.mdi.sim.task.TaskHandle;
import edu.cnu.mdi.sim.task.TaskListener;

class BopitCalculationTaskTest {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void taskRunsCalculationAndReturnsTypedOutcome() throws Exception {
        var constants = PublishedConstantSets.BOPIT_1990;
        var problem = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        TaskHandle<BopitCalculationOutcome> task = BackgroundTasks.create(
                new BopitCalculationTask(problem, constants));
        CountDownLatch terminated = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        task.addListener(new TaskListener<>() {
            @Override public void onFailed(TaskHandle<BopitCalculationOutcome> source,
                    Throwable error) {
                failure.set(error);
            }
            @Override public void onCompleted(TaskHandle<BopitCalculationOutcome> source,
                    CompletionStatus status, Throwable error) {
                terminated.countDown();
            }
        });

        task.start();
        assertTrue(terminated.await(10, TimeUnit.SECONDS), "background calculation timed out");
        assertNull(failure.get());

        var outcome = (BopitCalculationOutcome.PointCoulomb) task.getResult();
        assertEquals(-0.367806279690, outcome.result().calculatedEnergyMeV(), 2e-12);
    }
}
