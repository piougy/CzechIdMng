package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;

/**
 * Deletes automatic role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component(RoleTreeNodeDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes automatic role.")
public class RoleTreeNodeDeleteProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	public RoleTreeNodeDeleteProcessor() {
		super(RoleTreeNodeEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto roleTreeNode = event.getContent();
		//
		if (roleTreeNode.getId() == null) {
			return new DefaultEventResult<>(event, this);
		}
		//
		// delete all assigned roles gained by this automatic role by long running task
		RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(roleTreeNode.getId());
		if (event.getPriority() == PriorityType.IMMEDIATE) {
			longRunningTaskManager.executeSync(automaticRoleTask);
			return new DefaultEventResult.Builder<>(event, this).build();
		}
		//
		automaticRoleTask.setContinueOnException(true);
		if (longRunningTaskManager.isAsynchronous()) {
			automaticRoleTask.setRequireNewTransaction(true);
		}
		try {
			longRunningTaskManager.execute(automaticRoleTask);
		} catch (AcceptedException ex) {
			DefaultEventResult<IdmRoleTreeNodeDto> result = new DefaultEventResult<>(event, this);
			result.setSuspended(true);
			//
			return result;
		}
		//
		return new DefaultEventResult.Builder<>(event, this).build();
	}
}
