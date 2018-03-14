package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.EntityEventEvent.EntityEventType;

/**
 * Start execution of entity event.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Component
@Description("Starts execution of entity event.")
public class EntityEventStartProcessor extends CoreEventProcessor<IdmEntityEventDto> {
	
	public static final String PROCESSOR_NAME = "entity-event-start-processor";
	//
	@Autowired private EntityEventManager entityEventManager;
	
	public EntityEventStartProcessor() {
		super(EntityEventType.EXECUTE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entity = event.getContent();
		//
		entity = entityEventManager.saveResult(entity.getId(), new OperationResultDto.Builder(OperationState.RUNNING).build());
		event.setContent(entity);
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
		// before process
		return -1000;
	}
}
