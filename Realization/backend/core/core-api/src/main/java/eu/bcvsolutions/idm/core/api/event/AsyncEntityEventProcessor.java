package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;

/**
 * Asynchronous entity event processor
 * - processor can control asynchronous processing priority 
 * 
 * @author Radek Tomiška
 *
 */
public interface AsyncEntityEventProcessor<E extends Serializable> extends EntityEventProcessor<E> {

	/**
	 * Registered async processor can vote about priority of processing for given event.
	 * Returns {@code null} by default => processor doesn't vote about priority - preserve original event priority. 
	 * Use {{@code PriorityType.IMMEDIATE}} to execute whole event synchronously.
	 * All registered processors votes about event priority - event will be processed with the highest priority.
	 * 
	 * @param event
	 * @return
	 */
	default PriorityType getPriority(EntityEvent<E> event) {
		return null;
	}
}
