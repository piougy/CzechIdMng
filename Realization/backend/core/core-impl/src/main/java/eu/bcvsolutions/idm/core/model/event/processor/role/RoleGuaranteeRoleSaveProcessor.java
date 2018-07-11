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
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;

/**
 * Persists role guarantee by role.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
@Component(RoleGuaranteeRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists role guarantee by role.")
public class RoleGuaranteeRoleSaveProcessor
		extends CoreEventProcessor<IdmRoleGuaranteeRoleDto> {
	
	public static final String PROCESSOR_NAME = "role-guarantee-role-save-processor";
	//
	@Autowired private IdmRoleGuaranteeRoleService service;
	
	public RoleGuaranteeRoleSaveProcessor() {
		super(RoleGuaranteeRoleEventType.UPDATE, RoleGuaranteeRoleEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleGuaranteeRoleDto> process(EntityEvent<IdmRoleGuaranteeRoleDto> event) {
		IdmRoleGuaranteeRoleDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
