package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
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
@Component
@Description("Deletes automatic role.")
public class RoleTreeNodeDeleteProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	private final LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmAutomaticRoleRequestService automaticRoleRequestService;
	
	@Autowired
	public RoleTreeNodeDeleteProcessor(
			IdmRoleRequestService roleRequestService,
			LongRunningTaskManager longRunningTaskManager) {
		super(RoleTreeNodeEventType.DELETE);
		//
		Assert.notNull(roleRequestService);
		Assert.notNull(longRunningTaskManager);
		//
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto roleTreeNode = event.getContent();
		
		// Find all automatic role requests and remove relation on automatic role
		if (roleTreeNode != null) {
			IdmAutomaticRoleRequestFilter automaticRoleRequestFilter = new IdmAutomaticRoleRequestFilter();
			automaticRoleRequestFilter.setAutomaticRoleId(roleTreeNode.getId());
			automaticRoleRequestFilter.setRequestType(AutomaticRoleRequestType.TREE);
			
			automaticRoleRequestService.find(automaticRoleRequestFilter, null).getContent().forEach(request -> {
				request.setAutomaticRole(null);
				automaticRoleRequestService.save(request);
			});
		}
		//
		// delete all assigned roles gained by this automatic role by long running task
		RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(roleTreeNode.getId());
		longRunningTaskManager.executeSync(automaticRoleTask);
		//
		return new DefaultEventResult<>(event, this);
	}
}
