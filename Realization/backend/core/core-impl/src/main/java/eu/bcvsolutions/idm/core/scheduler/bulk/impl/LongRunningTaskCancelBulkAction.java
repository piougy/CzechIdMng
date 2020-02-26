package eu.bcvsolutions.idm.core.scheduler.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Cancel created or running long running task.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component(LongRunningTaskCancelBulkAction.NAME)
@Description("Cancel created or running long running task.")
public class LongRunningTaskCancelBulkAction extends AbstractBulkAction<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> {

	public static final String NAME = "core-long-running-task-cancel-bulk-action";
	//
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private LongRunningTaskManager manager;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected OperationResult processDto(IdmLongRunningTaskDto dto) {
		manager.cancel(dto.getId());
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCHEDULER_UPDATE);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 200;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return NotificationLevel.WARNING;
	}

	@Override
	public ReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> getService() {
		return service;
	}
}
