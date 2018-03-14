package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.EntityEventEvent;

/**
 * Publish entity change
 * 
 * @author Radek Tomiška
 *
 * @param <DTO> an abstract dto
 * @since 8.0.0
 */
public abstract class AbstractPublishEntityChangeProcessor <DTO extends AbstractDto> extends CoreEventProcessor<DTO> {
	
	@Autowired private EntityEventManager entityEventManager;
	
	public AbstractPublishEntityChangeProcessor(EventType... type) {
		super(type);
	}
	
	@Override
	public EventResult<DTO> process(EntityEvent<DTO> event) {
		entityEventManager.changedEntity(event.getContent(), event);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return EntityEventEvent.DEFAULT_ORDER;
	}
}
