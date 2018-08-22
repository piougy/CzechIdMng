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
import eu.bcvsolutions.idm.core.api.event.RoleGuaranteeRoleEvent.RoleGuaranteeRoleEventType;
import eu.bcvsolutions.idm.core.api.event.processor.RoleGuaranteeProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;

/**
 * Persists role guarantee by identity.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(RoleGuaranteeSaveProcessor.PROCESSOR_NAME)
@Description("Persists role guarantee by identity.")
public class RoleGuaranteeSaveProcessor
		extends CoreEventProcessor<IdmRoleGuaranteeDto> 
		implements RoleGuaranteeProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-guarantee-save-processor";
	//
	@Autowired private IdmRoleGuaranteeService service;
	
	public RoleGuaranteeSaveProcessor() {
		super(RoleGuaranteeEventType.UPDATE, RoleGuaranteeRoleEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleGuaranteeDto> process(EntityEvent<IdmRoleGuaranteeDto> event) {
		IdmRoleGuaranteeDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
