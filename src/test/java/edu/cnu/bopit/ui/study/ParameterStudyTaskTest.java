package edu.cnu.bopit.ui.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import edu.cnu.bopit.calculation.PublishedProblems;
import edu.cnu.bopit.physics.constants.PublishedConstantSets;
import edu.cnu.bopit.study.AxisValue;
import edu.cnu.bopit.study.ExplicitParameterAxis;
import edu.cnu.bopit.study.ParameterStudy;
import edu.cnu.bopit.study.ParameterStudyResult;
import edu.cnu.bopit.study.ParameterTarget;
import edu.cnu.bopit.study.StudyObservable;
import edu.cnu.bopit.study.StudyPointStatus;
import edu.cnu.mdi.sim.CompletionStatus;
import edu.cnu.mdi.sim.task.BackgroundTasks;
import edu.cnu.mdi.sim.task.TaskHandle;
import edu.cnu.mdi.sim.task.TaskListener;

class ParameterStudyTaskTest {
	static {
		System.setProperty("java.awt.headless", "true");
	}

    @Test
    void returnsCompletedStudyThroughTypedTaskHandle() throws Exception {
        var constants = PublishedConstantSets.BOPIT_1990;
        var base = PublishedProblems.kaonicSulfur32Legacy3d(constants, 40, 10);
        var axis = new ExplicitParameterAxis("points", ParameterTarget.TOTAL_GRID_POINTS,
                List.of(new AxisValue("20", 20), new AxisValue("21 invalid", 21)));
        var study = new ParameterStudy("task test", base, List.of(axis),
                List.of(StudyObservable.BINDING_ENERGY_MEV));
        TaskHandle<ParameterStudyResult> task = BackgroundTasks.create(
                new ParameterStudyTask(study, constants));
        CountDownLatch completed = new CountDownLatch(1);
        task.addListener(new TaskListener<>() {
            @Override public void onCompleted(TaskHandle<ParameterStudyResult> source,
                    CompletionStatus status, Throwable error) {
                completed.countDown();
            }
        });

        task.start();
        assertTrue(completed.await(10, TimeUnit.SECONDS), "parameter study timed out");
        assertEquals(CompletionStatus.SUCCEEDED, task.getCompletionStatus());
        ParameterStudyResult result = task.getResult();
        assertFalse(result.cancelled());
        assertEquals(2, result.points().size());
        assertEquals(StudyPointStatus.SUCCESS, result.points().get(0).status());
        assertEquals(StudyPointStatus.FAILED, result.points().get(1).status());
    }
}
