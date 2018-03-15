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
 * Persists entity event.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Component
@Description("Persists entity event.")
public class EntityEventSaveProcessor
		extends CoreEventProcessor<IdmEntityEventDto> {
	
	public static final String PROCESSOR_NAME = "entity-event-save-processor";
	//
	@Autowired private IdmEntityEventService service;
	
	public EntityEventSaveProcessor() {
		super(EntityEventType.UPDATE, EntityEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
