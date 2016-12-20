package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;

/**
 * Entity processing based on spring plugins
 * 
 * @see {@link EntityEventProcessor}
 * @see {@link EntityEvent}
 * @see {@link EventContext}
 * 
 * @author Radek Tomiška
 *
 */
public interface EntityEventManager {
	
	/**
	 * Process event through all registered entity processor in configured order with default context (newly created context).
	 * 
	 * @param event
	 * @return
	 */
	<E extends BaseEntity> EventContext<E> process(EntityEvent<E> event);
	
	// TODO: get listeners
}
