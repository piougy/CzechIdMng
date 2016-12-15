package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

/**
 * Entity processing based on event publishing.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultEntityEventManager implements EntityEventManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventManager.class);
	private final ApplicationEventPublisher publisher;
	
	@Autowired
	public DefaultEntityEventManager(ApplicationEventPublisher publisher) {
		Assert.notNull(publisher, "Event publisher is required");
		//
		this.publisher = publisher;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends BaseEntity> EventContext<E> process(EntityEvent<E> event) {
		Assert.notNull(event);
		//
		LOG.debug("Publishing event [{}] [{}]", event.getContent().getClass().getSimpleName(), event.getType());
		this.publisher.publishEvent(event); 
		return event.getContext();
	}	
}
