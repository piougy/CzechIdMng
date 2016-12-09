package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Event state holder (content + metadata)
 * 
 * TODO: Split event and context!
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public interface EntityEvent<E extends AbstractEntity> {

	/**
	 * Operation type
	 * 
	 * @return
	 */
	EventType<E> getType();
	
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
}
