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
}
