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
 * Deletes incompatible role.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(IncompatibleRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes incompatible role from repository.")
public class IncompatibleRoleDeleteProcessor
		extends CoreEventProcessor<IdmIncompatibleRoleDto>
		implements IncompatibleRoleProcessor{
	
	public static final String PROCESSOR_NAME = "core-incompatible-role-delete-processor";
	//
	@Autowired private IdmIncompatibleRoleService service;
	
	public IncompatibleRoleDeleteProcessor() {
		super(IncompatibleRoleEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIncompatibleRoleDto> process(EntityEvent<IdmIncompatibleRoleDto> event) {
		service.deleteInternal(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
}