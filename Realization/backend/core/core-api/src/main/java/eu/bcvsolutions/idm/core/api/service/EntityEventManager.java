package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;

/**
 * Entity processing based on spring plugins
 * 
 * @author Radek Tomiška
 * @see {@link EntityEventProcessor}
 * @see {@link EntityEvent}
 * @see {@link EventContext}
 */
public interface EntityEventManager {
	
	/**
	 * Process event through all registered entity processor in configured order with default context (newly created context).
	 * 
	 * @param event
	 * @return
	 */
	<E extends Serializable> EventContext<E> process(EntityEvent<E> event);
	
	
	/**
	 * Returns all registered entity event processors
	 * 
	 * @param filter
	 * @return
	 */
	List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter);
}
