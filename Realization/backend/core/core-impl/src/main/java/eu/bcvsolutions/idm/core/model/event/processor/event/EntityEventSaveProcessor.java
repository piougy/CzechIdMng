package eu.bcvsolutions.idm.core.model.event.processor.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
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
	@Autowired private EntityEventManager entityEventManager;
	
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
		// check parent event to be completed
		if (event.getOriginalSource() != null 
				&& event.getOriginalSource().getResult().getState().isRunnable()
				&& !entity.getResult().getState().isRunnable() 
				&& entity.getRootId() != null) {
			IdmEntityEventDto rootEvent = service.get(entity.getRootId());
			if (rootEvent != null && rootEvent.getResult().getState().isRunnable()) { // created / running
				// check all parent children and complete
				IdmEntityEventFilter filter = new IdmEntityEventFilter();
				filter.setRootId(rootEvent.getId());
				List<OperationState> running = Lists.newArrayList(OperationState.CREATED, OperationState.RUNNING); 
				filter.setStates(running);
				if (service.find(filter, new PageRequest(0, 1)).getTotalElements() == 0) {
					// set as executed
					rootEvent = service.get(rootEvent.getId());
					if (rootEvent.getResult().getState().isRunnable()) { // created / running
						// TODO: check sub items results and set different state?
						rootEvent.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).build());
						rootEvent = service.save(rootEvent);
						// publish notify event
						EntityEvent<? extends Identifiable> notifyEvent = entityEventManager.toEvent(rootEvent);
						entityEventManager.changedEntity(notifyEvent.getContent(), notifyEvent);
					}
				}
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}
