package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Event context state holder (event results + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public interface EventContext<E extends Serializable> extends Serializable {
	
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
	 * Event is suspended = no other events will be processed, while event is suspended. 
	 * Suspended event could be republished again - when will continue when event was suspended - all processors 
	 * with greater order than getProcessedOrder will be called.
	 * 
	 * @return
	 */
	boolean isSuspended();
	
	/**
	 * Sets suspended
	 * 
	 * @see {@link #isSuspended()}
	 * @param suspended
	 */
	void setSuspended(boolean suspended);

	/**
	 * Returns last event result, or null, if no event was processed.
	 * 
	 * @return
	 */
	EventResult<E> getLastResult();
	
	/**
	 * Returns last processed order or {@code null}, if any processor was called (event is starting).
	 * 
	 * @return
	 */
	Integer getProcessedOrder();
	
	/**
	 * Sets last processed order or {@code null}, if any processor was called (event will start again).
	 * 
	 * @param processedOrder
	 */
	void setProcessedOrder(Integer processedOrder);
}
