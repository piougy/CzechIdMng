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
	 * {@link AbstractEntity} type
	 * 
	 * @return
	 */
	Class<E> getEntityClass();

	/**
	 * Operation type
	 * 
	 * @return
	 */
	String getOperation();
	
	/**
	 * Event content - entity affected by action
	 *  
	 * @return
	 */
	E getContent();
	
	/**
	 * Event content - entity affected by action
	 * 
	 * @param content
	 */
	void setContent(E content);

	/**
	 * Event is completed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	boolean isComplete();

	/**
	 * Event is completed = no other events will be processed (break event chain)
	 */
	void setComplete(boolean complete);
	
	/**
	 * Event properties (metadata)
	 * 
	 * @return
	 */
	Map<String, Serializable> getProperties();
}
