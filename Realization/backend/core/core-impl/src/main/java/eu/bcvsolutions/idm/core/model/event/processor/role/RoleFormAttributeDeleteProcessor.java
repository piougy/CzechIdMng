package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.RoleFormAttributeEvent.RoleFormAttributeEventType;
import eu.bcvsolutions.idm.core.api.event.processor.RoleFormAttributeProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;

/**
 * Delete processor for relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition" - ensures referential integrity.
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleFormAttributeDeleteProcessor.PROCESSOR_NAME)
@Description(" Delete processor for relation between role and definition of form-attribution - ensures referential integrity.")
public class RoleFormAttributeDeleteProcessor
		extends CoreEventProcessor<IdmRoleFormAttributeDto>
		implements RoleFormAttributeProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-form-attribute-delete-processor";
	@Autowired private IdmRoleFormAttributeService service;
	
	public RoleFormAttributeDeleteProcessor() {
		super(RoleFormAttributeEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleFormAttributeDto> process(EntityEvent<IdmRoleFormAttributeDto> event) {
		IdmRoleFormAttributeDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}