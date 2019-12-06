package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.List;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.rest.impl.IdmLongRunningTaskController;
import eu.bcvsolutions.idm.core.scheduler.task.impl.TestTaskExecutor;

/**
 * Controller tests
 * - read long running tasks
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class IdmLongRunningTaskControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmLongRunningTaskDto> {

	@Autowired private IdmLongRunningTaskController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmLongRunningTaskDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmLongRunningTaskDto prepareDto() {
		TestTaskExecutor taskExecutor = new TestTaskExecutor(); 
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskProperties(taskExecutor.getProperties());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId("mock");
		task.setResult(new OperationResult.Builder(OperationState.BLOCKED).build());
		//
		return task;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsPost() {
		return false;
	}
	
	@Override
	protected boolean supportsPut() {
		return false;
	}
	
	@Test
	public void testFindByCreated() {
		String mockInstanceId = getHelper().createName();
		IdmLongRunningTaskDto task = prepareDto();
		task.setInstanceId(mockInstanceId);
		IdmLongRunningTaskDto taskOne = createDto(task);
		//
		getHelper().waitForResult(null, 1, 1); // created is filled automatically
		ZonedDateTime middle = ZonedDateTime.now();
		getHelper().waitForResult(null, 1, 1);
		//
		task = prepareDto();
		task.setInstanceId(mockInstanceId);
		IdmLongRunningTaskDto taskTwo = createDto(task);
		
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setInstanceId(mockInstanceId);
		//
		filter.setFrom(middle);
		List<IdmLongRunningTaskDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskTwo.getId())));
		//
		filter.setFrom(null);
		filter.setTill(middle);
		results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
		//
		filter.setFrom(taskOne.getCreated().truncatedTo(ChronoUnit.MILLIS));
		filter.setTill(taskTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS));
		results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskTwo.getId())));
	}
}
