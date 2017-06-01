package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AddNewAutomaticRoleTaskExecutor;

/**
 * Persists automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists automatic role.")
public class RoleTreeNodeSaveProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-save-processor";
	@Autowired
	private IdmRoleTreeNodeService service;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	
	public RoleTreeNodeSaveProcessor() {
		super(RoleTreeNodeEventType.CREATE); // update is not supported
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto dto = event.getContent();
		//
		dto = service.saveInternal(dto);
		event.setContent(dto);
		//
		// assign role by this added automatic role to all existing identity contracts with long running task
		AddNewAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(AddNewAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setRoleTreeNode(dto);
		longRunningTaskManager.execute(automaticRoleTask);
		return new DefaultEventResult<>(event, this);
	}
}
