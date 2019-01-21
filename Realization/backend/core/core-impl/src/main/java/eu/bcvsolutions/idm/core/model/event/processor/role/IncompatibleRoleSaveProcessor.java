package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IncompatibleRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.model.event.IncompatibleRoleEvent.IncompatibleRoleEventType;

/**
 * Persists incompatible role
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(IncompatibleRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists incompatible role.")
public class IncompatibleRoleSaveProcessor
		extends CoreEventProcessor<IdmIncompatibleRoleDto> 
		implements IncompatibleRoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-incompatible-role-save-processor";
	//
	@Autowired private IdmIncompatibleRoleService service;
	
	public IncompatibleRoleSaveProcessor() {
		super(IncompatibleRoleEventType.CREATE); // update is not supported
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIncompatibleRoleDto> process(EntityEvent<IdmIncompatibleRoleDto> event) {
		IdmIncompatibleRoleDto incompatibleRole = event.getContent();
		incompatibleRole = service.saveInternal(incompatibleRole);
		event.setContent(incompatibleRole);
		//
		return new DefaultEventResult<>(event, this);
	}
}
