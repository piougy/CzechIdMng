package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - persist role.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRoleSaveProcessor.PROCESSOR_NAME)
@Description("Duplicate role - persist role.")
public class DuplicateRoleSaveProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-save-processor";
	//
	@Autowired private IdmRoleService service;
	
	public DuplicateRoleSaveProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto cloned = event.getContent();
		//
		cloned = service.save(cloned);
		event.setContent(cloned);
		//
		return new DefaultEventResult<>(event, this);
	}

}
