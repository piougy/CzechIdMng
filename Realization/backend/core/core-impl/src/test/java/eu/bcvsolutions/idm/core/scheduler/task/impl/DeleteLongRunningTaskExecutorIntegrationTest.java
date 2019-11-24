package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteLongRunningTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testDeleteOldLongRunningTasks() {
		// prepare provisioning operations
		ZonedDateTime createdOne = ZonedDateTime.now().minusDays(2);
		UUID ownerId = UUID.randomUUID();
		IdmLongRunningTaskDto operationOne = createDto(ownerId, createdOne, OperationState.EXECUTED);
		// all other variants for not removal
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.CREATED);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED);
		createDto(ownerId, ZonedDateTime.now().minusDays(2), OperationState.EXCEPTION);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(23), OperationState.EXECUTED);
		//
		Assert.assertEquals(createdOne, operationOne.getCreated());
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setInstanceId(ownerId.toString());
		List<IdmLongRunningTaskDto> lrts = service.find(filter, null).getContent();
		Assert.assertEquals(6, lrts.size());
		//
		DeleteLongRunningTaskExecutor taskExecutor = new DeleteLongRunningTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteLongRunningTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteLongRunningTaskExecutor.PARAMETER_OPERATION_STATE, OperationState.EXECUTED);
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		lrts = service.find(filter, null).getContent();
		Assert.assertEquals(5, lrts.size());
		Assert.assertTrue(lrts.stream().allMatch(a -> !a.getId().equals(operationOne.getId())));		
	}
	
	private IdmLongRunningTaskDto createDto(UUID ownerId, ZonedDateTime created, OperationState state) {
		TestTaskExecutor taskExecutor = new TestTaskExecutor(); 
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setCreated(created);
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskProperties(taskExecutor.getProperties());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId(ownerId.toString());
		task.setResult(new OperationResult.Builder(state).build());
		//
		return service.save(task);
	}
}
