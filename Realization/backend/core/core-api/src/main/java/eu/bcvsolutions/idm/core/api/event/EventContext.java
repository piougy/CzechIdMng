package eu.bcvsolutions.idm.core.api.event;

import java.util.List;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Event context state holder (event results + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public interface EventContext<E extends AbstractEntity> {
	
	/**
	 * Already processed events
	 * 
	 * @return
	 */
	List<EventResult<E>> getResults();
	
	/**
	 * Add result 
	 * 
	 * @param eventResult
	 */
	void addResult(EventResult<E> eventResult);
	
	/**
	 * Returns last event content, or null, if no event was processed.
	 * 
	 * @return
	 */
	E getContent();
}
