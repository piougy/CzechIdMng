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
 * Persists entity state.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists entity state.")
public class EntityStateSaveProcessor
		extends CoreEventProcessor<IdmEntityStateDto> 
		implements EntityStateProcessor {
	
	public static final String PROCESSOR_NAME = "entity-state-save-processor";
	//
	@Autowired private IdmEntityStateService service;
	
	public EntityStateSaveProcessor() {
		super(EntityStateEventType.UPDATE, EntityStateEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityStateDto> process(EntityEvent<IdmEntityStateDto> event) {
		IdmEntityStateDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}

}
