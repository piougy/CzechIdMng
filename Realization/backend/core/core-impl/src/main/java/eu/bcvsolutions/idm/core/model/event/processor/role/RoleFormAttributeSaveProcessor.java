package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.RoleGuaranteeEvent.RoleGuaranteeEventType;
import eu.bcvsolutions.idm.core.api.event.RoleGuaranteeRoleEvent.RoleGuaranteeRoleEventType;
import eu.bcvsolutions.idm.core.api.event.processor.RoleFormAttributeProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;

/**
 * Save processor for relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleFormAttributeSaveProcessor.PROCESSOR_NAME)
@Description("Save processor for relation between role and definition of form-attribution.")
public class RoleFormAttributeSaveProcessor
		extends CoreEventProcessor<IdmRoleFormAttributeDto> 
		implements RoleFormAttributeProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-form-attribute-save-processor";
	//
	@Autowired private IdmRoleFormAttributeService service;
	
	public RoleFormAttributeSaveProcessor() {
		super(RoleGuaranteeEventType.UPDATE, RoleGuaranteeRoleEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleFormAttributeDto> process(EntityEvent<IdmRoleFormAttributeDto> event) {
		IdmRoleFormAttributeDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
