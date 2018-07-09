package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;

/**
 * Deletes entity event - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes entity event from repository.")
public class EntityEventDeleteProcessor
		extends CoreEventProcessor<IdmEntityEventDto> {
	
	public static final String PROCESSOR_NAME = "entity-event-delete-processor";
	@Autowired private IdmEntityEventService service;
	
	public EntityEventDeleteProcessor() {
		super(EntityEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}