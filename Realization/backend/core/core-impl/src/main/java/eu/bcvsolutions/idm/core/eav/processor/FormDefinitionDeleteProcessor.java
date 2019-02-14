package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent.FormDefinitionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormDefinitionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Deletes form definition - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component
@Description("Deletes form definition from repository.")
public class FormDefinitionDeleteProcessor
		extends CoreEventProcessor<IdmFormDefinitionDto> 
		implements FormDefinitionProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-definition-delete-processor";
	@Autowired private IdmFormDefinitionService service;
	
	public FormDefinitionDeleteProcessor() {
		super(FormDefinitionEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormDefinitionDto> process(EntityEvent<IdmFormDefinitionDto> event) {
		IdmFormDefinitionDto entityState = event.getContent();
		//		
		service.deleteInternal(entityState);
		//
		return new DefaultEventResult<>(event, this);
	}
}