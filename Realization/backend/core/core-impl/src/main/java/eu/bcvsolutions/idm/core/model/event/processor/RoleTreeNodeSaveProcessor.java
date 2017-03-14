package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Persists automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Creates automatic role.")
public class RoleTreeNodeSaveProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-save-processor";
	private final IdmRoleTreeNodeService service;
	
	@Autowired
	public RoleTreeNodeSaveProcessor(IdmRoleTreeNodeService service) {
		super(RoleTreeNodeEventType.CREATE); // update is not supported
		//
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto dto = event.getContent();
		//
		event.setContent(service.saveInternal(dto));
		//		
		return new DefaultEventResult<>(event, this);
	}
}
