package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.exception.EventContentDeletedException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

/**
 * Execute entity event.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Component
@Description("Execute entity event.")
public class EntityEventExecuteProcessor extends CoreEventProcessor<IdmEntityEventDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EntityEventExecuteProcessor.class);
	public static final String PROCESSOR_NAME = "entity-event-execute-processor";
	//
	@Autowired private EntityEventManager entityEventManager;
	
	public EntityEventExecuteProcessor() {
		super(EntityEventType.EXECUTE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entityEvent = event.getContent();
		//
		EntityEvent<Identifiable> resurectedEvent;
		try {
			resurectedEvent = entityEventManager.toEvent(entityEvent);
			// execute
			EventContext<Identifiable> context = entityEventManager.process(resurectedEvent);
			entityEvent.setProcessedOrder(context.getProcessedOrder());
			entityEvent.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).build());
		} catch (EventContentDeletedException ex) {
			// content was deleted - log state
			LOG.warn("Event content was deleted, event cannot be executed.", ex);
			entityEvent.setResult(new OperationResultDto
					.Builder(OperationState.NOT_EXECUTED) // it's expected ex, lower level 
					.setException(ex)
					.build());
		}
		//
		event.setContent(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
	
}
