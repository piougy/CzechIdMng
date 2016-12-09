package eu.bcvsolutions.idm.core.api.event;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Entity event processor result
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public interface EventResult<E extends AbstractEntity> {
	
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
	 * Event is completed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	boolean isCompleted();
}
