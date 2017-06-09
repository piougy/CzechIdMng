package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity event processor result
 * 
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Identifiable} content type
 * @author Radek Tomiška
 */
public interface EventResult<E extends Serializable> extends Serializable {
	
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
