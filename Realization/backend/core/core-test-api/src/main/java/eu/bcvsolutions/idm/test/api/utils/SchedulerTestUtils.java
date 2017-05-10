package eu.bcvsolutions.idm.test.api.utils;

import static org.junit.Assert.assertEquals;

import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;

public class SchedulerTestUtils {

	public static IdmScheduledTaskDto createIdmScheduledTask(String taskName) {
		IdmScheduledTaskDto t = new IdmScheduledTaskDto();
		t.setQuartzTaskName(taskName);
		t.setDryRun(false);
		return t;
	}

	public static IdmLongRunningTaskDto createIdmLongRunningTask(IdmScheduledTaskDto taskDto,
			Class<? extends SchedulableTaskExecutor<Boolean>> clazz) {
		//
		IdmLongRunningTaskDto longRunningTask = new IdmLongRunningTaskDto();
		longRunningTask.setTaskType(clazz.getCanonicalName()); 
		longRunningTask.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		longRunningTask.setInstanceId("test_instance");
		longRunningTask.setScheduledTask(taskDto.getId());
		longRunningTask.setRunning(true);
		//
		return longRunningTask;
	}
	
	public static <E extends AbstractDto> void checkQueueItems(IdmScheduledTaskDto scheduledTask,
			Class<E> dtoClazz, Page<IdmProcessedTaskItemDto> queueItems) {
		//
		for (IdmProcessedTaskItemDto qi : queueItems) {
			assertEquals(scheduledTask.getId(), qi.getScheduledTaskQueueOwner());
			assertEquals(dtoClazz.getCanonicalName(), qi.getReferencedDtoType());
			assertEquals(OperationState.EXECUTED, qi.getOperationResult().getState());
		}
	}

	public static <E extends AbstractDto> void checkLogItems(IdmLongRunningTaskDto longRunningTask,
			Class<E> dtoClazz, Page<IdmProcessedTaskItemDto> logItems) {
		//
		for (IdmProcessedTaskItemDto qi : logItems) {
			assertEquals(longRunningTask.getId(), qi.getLongRunningTask());
			assertEquals(dtoClazz.getCanonicalName(), qi.getReferencedDtoType());
			assertEquals(OperationState.EXECUTED, qi.getOperationResult().getState());
		}
	}

}
