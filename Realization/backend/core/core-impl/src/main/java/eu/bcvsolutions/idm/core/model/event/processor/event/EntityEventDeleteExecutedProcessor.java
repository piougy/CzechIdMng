package eu.bcvsolutions.idm.core.model.event.processor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityEventRepository;

/**
 * Delete successfully executed entity events.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Component(EntityEventDeleteExecutedProcessor.PROCESSOR_NAME)
@Description("Delete successfully executed entity events.")
public class EntityEventDeleteExecutedProcessor extends CoreEventProcessor<IdmEntityEventDto> {
	
	public static final String PROCESSOR_NAME = "entity-event-delete-executed-processor";
	//
	@Autowired private IdmEntityEventService service;
	@Autowired private IdmEntityEventRepository repository;
	
	public EntityEventDeleteExecutedProcessor() {
		super(EntityEventType.EXECUTE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmEntityEventDto> process(EntityEvent<IdmEntityEventDto> event) {
		IdmEntityEventDto entityEvent = event.getContent();
		if (OperationState.isSuccessful(entityEvent.getResult().getState())) {
			if (repository.countByParentId(entityEvent.getId()) == 0) {
				service.delete(entityEvent);
			}
		}
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
		// after end process
		return 5000;
	}
}
