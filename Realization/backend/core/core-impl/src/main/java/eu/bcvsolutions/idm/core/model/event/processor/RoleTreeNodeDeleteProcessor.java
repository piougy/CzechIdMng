package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;

/**
 * Deletes automatic role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes automatic role.")
public class RoleTreeNodeDeleteProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	private final IdmRoleTreeNodeService roleTreeNodeService;
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public RoleTreeNodeDeleteProcessor(
			IdmRoleRequestService roleRequestService,
			IdmRoleTreeNodeService roleTreeNodeService,
			LongRunningTaskManager longRunningTaskManager) {
		super(RoleTreeNodeEventType.DELETE);
		//
		Assert.notNull(roleRequestService);
		Assert.notNull(roleTreeNodeService);
		Assert.notNull(longRunningTaskManager);
		//
		this.roleTreeNodeService = roleTreeNodeService;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto roleTreeNode = event.getContent();
		//
		// delete all assigned roles gained by this automatic role by long running task
		// TODO: optional remove by logged user input
		RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setRoleTreeNode(roleTreeNodeService.toEntity(roleTreeNode, null));
		longRunningTaskManager.execute(automaticRoleTask);
		//
		return new DefaultEventResult<>(event, this);
	}
}
