package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;

/**
 * Entity processing based on spring plugins
 * 
 * @see {@link EntityEventProcessor}
 * @see {@link EntityEvent}
 * 
 * @author Radek Tomiška
 *
 */
public interface EntityEventProcessorService {
	
	/**
	 * Process event through all registered entity processor in configured order
	 * 
	 * @param context
	 * @return
	 */
	<E extends AbstractEntity> EntityEvent<E> process(EntityEvent<E> context);

}
