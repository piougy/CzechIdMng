package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.RoleGuaranteeEvent.RoleGuaranteeEventType;
import eu.bcvsolutions.idm.core.api.event.processor.RoleGuaranteeProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;

/**
 * Deletes role guarante by identity - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(RoleGuaranteeDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes role guarante by identity - ensures referential integrity.")
public class RoleGuaranteeDeleteProcessor
		extends CoreEventProcessor<IdmRoleGuaranteeDto>
		implements RoleGuaranteeProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-guarantee-delete-processor";
	@Autowired private IdmRoleGuaranteeService service;
	
	public RoleGuaranteeDeleteProcessor() {
		super(RoleGuaranteeEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleGuaranteeDto> process(EntityEvent<IdmRoleGuaranteeDto> event) {
		IdmRoleGuaranteeDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}