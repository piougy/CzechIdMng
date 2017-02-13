package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.ResolvableTypeProvider;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Event state holder (content + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public interface EntityEvent<E extends BaseEntity> extends ResolvableTypeProvider {

	/**
	 * Operation type
	 * 
	 * @return
	 */
	EventType getType();
	
	/**
	 * Event content - entity. Could not be null. Events with empty content could not be processed.
	 *  
	 * @return
	 */
	E getContent();
	
	/**
	 * Event properties (metadata)
	 * 
	 * @return
	 */
	Map<String, Serializable> getProperties();
	
	/**
	 * Event context
	 * 
	 * @return
	 */
	EventContext<E> getContext();
	
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
	 * Returns last processed order or {@code null}, if any processor was called (event is starting).
	 * 
	 * @return
	 */
	Integer getProcessedOrder();
}
