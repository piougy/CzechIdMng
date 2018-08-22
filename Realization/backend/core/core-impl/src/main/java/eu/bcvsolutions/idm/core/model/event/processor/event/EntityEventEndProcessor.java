package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;

/**
 * End execution of entity event - persist state only.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Component
@Description("Ends execution of entity event.")
public class EntityEventEndProcessor extends CoreEventProcessor<IdmEntityEventDto> {
	
	public static final String PROCESSOR_NAME = "entity-event-end-processor";
	//
	@Autowired private IdmEntityEventService service;
	
	public EntityEventEndProcessor() {
		super(EntityEventType.EXECUTE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entityEvent = event.getContent();
		entityEvent = service.save(entityEvent);
		event.setContent(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Asynchronous processing can be disabled
	 */
	@Override
	public boolean conditional(EntityEvent<IdmEntityEventDto> event) {
		return event.getContent().getId() != null;
	}
	
	@Override
	public int getOrder() {
		// after process
		return 1000;
	}
}
