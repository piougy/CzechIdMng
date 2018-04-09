package eu.bcvsolutions.idm.core.api.event.processor;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

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
	public boolean conditional(EntityEvent<DTO> event) {
		return super.conditional(event) 
				&& !getBooleanProperty(EntityEventManager.EVENT_PROPERTY_SKIP_NOTIFY, event.getProperties());
	}
	
	@Override
	public EventResult<DTO> process(EntityEvent<DTO> event) {
		// set additional props to event
		event = setAdditionalEventProperties(event);
		// publish notify event
		entityEventManager.changedEntity(event.getContent(), event);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Construct additional event properties for NOTIFY event.
	 * 
	 * @return
	 */
	protected EntityEvent<DTO> setAdditionalEventProperties(EntityEvent<DTO> event) {
		return event;
	}

	@Override
	public int getOrder() {
		return EntityEventEvent.DEFAULT_ORDER;
	}
}
