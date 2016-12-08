package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
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
public interface EntityEventProcessorManager {
	
	/**
	 * Process event through all registered entity processor in configured order with default context (newly created context).
	 * 
	 * @param event
	 * @return
	 */
	 <E extends AbstractEntity> EventContext<E> process(EntityEvent<E> event);
	
	/**
	 * Process event through all registered entity processor in configured order with given context.
	 * 
	 * @param event
	 * @param context
	 * @return
	 */
	 <E extends AbstractEntity> EventContext<E> process(EntityEvent<E> event, EventContext<E> context);

}
