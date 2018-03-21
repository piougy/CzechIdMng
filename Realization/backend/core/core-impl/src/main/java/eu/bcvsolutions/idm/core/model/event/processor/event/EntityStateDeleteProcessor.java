package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EntityStateEvent.EntityStateEventType;
import eu.bcvsolutions.idm.core.api.event.processor.EntityStateProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;

/**
 * Deletes entity state - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes entity state from repository.")
public class EntityStateDeleteProcessor
		extends CoreEventProcessor<IdmEntityStateDto> 
		implements EntityStateProcessor {
	
	public static final String PROCESSOR_NAME = "entity-state-delete-processor";
	@Autowired private IdmEntityStateService service;
	
	public EntityStateDeleteProcessor() {
		super(EntityStateEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityStateDto> process(EntityEvent<IdmEntityStateDto> event) {
		IdmEntityStateDto role = event.getContent();
		//		
		service.deleteInternal(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}