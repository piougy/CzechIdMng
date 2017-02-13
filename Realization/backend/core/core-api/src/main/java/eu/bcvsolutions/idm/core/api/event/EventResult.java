package eu.bcvsolutions.idm.core.api.event;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity event processor result
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public interface EventResult<E extends BaseEntity> {
	
	/**
	 * Processed event
	 * 
	 * @return
	 */
	EntityEvent<E> getEvent();
	
	/**
	 * Used processor
	 * 
	 * @return
	 */
	EntityEventProcessor<E> getProcessor();
	
	/**
	 * Event is closed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	boolean isClosed();
	
	/**
	 * Event is suspended = no other events will be processed, while event is suspended. 
	 * Suspended event could be republished again - when will continue when event was suspended.
	 * 
	 * @return
	 */
	boolean isSuspended();
	
	/**
	 * Returns last processed order
	 * 
	 * @return
	 */
	int getProcessedOrder();
}
