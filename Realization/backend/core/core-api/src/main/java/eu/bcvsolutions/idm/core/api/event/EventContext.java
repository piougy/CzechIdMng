package eu.bcvsolutions.idm.core.api.event;

import java.util.List;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Event context state holder (event results + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public interface EventContext<E extends BaseEntity> {
	
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
	
	/**
	 * Event is closed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	boolean isClosed();

	/**
	 * Returns last event result, or null, if no event was processed.
	 * 
	 * @return
	 */
	EventResult<E> getLastResult();
}
