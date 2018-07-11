package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.RoleGuaranteeRoleEvent.RoleGuaranteeRoleEventType;
import eu.bcvsolutions.idm.core.api.event.processor.RoleGuaranteeRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;

/**
 * Deletes role guarante by role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
@Component(RoleGuaranteeRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes role guarante by role from repository.")
public class RoleGuaranteeRoleDeleteProcessor
		extends CoreEventProcessor<IdmRoleGuaranteeRoleDto>
		implements RoleGuaranteeRoleProcessor {
	
	public static final String PROCESSOR_NAME = "role-guarantee-role-delete-processor";
	@Autowired private IdmRoleGuaranteeRoleService service;
	
	public RoleGuaranteeRoleDeleteProcessor() {
		super(RoleGuaranteeRoleEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleGuaranteeRoleDto> process(EntityEvent<IdmRoleGuaranteeRoleDto> event) {
		IdmRoleGuaranteeRoleDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}