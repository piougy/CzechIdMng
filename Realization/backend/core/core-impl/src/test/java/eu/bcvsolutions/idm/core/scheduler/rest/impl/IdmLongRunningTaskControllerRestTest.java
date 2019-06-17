package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
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
		task.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		//
		return task;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
}
