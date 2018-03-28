package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.ResolvableTypeProvider;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Event state holder (content + metadata)
 * 
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 * @author Radek Tomiška
 */
public interface EntityEvent<E extends Serializable> extends ResolvableTypeProvider, Serializable {
	public static final String EVENT_PROPERTY = "entityEvent";

	/**
	 * Operation type
	 * 
	 * @return
	 */
	EventType getType();

	/**
	 * Starting event content =~ source entity. Could not be null. Events with empty content could not be processed.
	 *  
	 * @return
	 */
	E getSource();
	
	/**
	 * Event content - entity. Could not be null. Events with empty content could not be processed.
	 *  
	 * @return
	 */
	E getContent();
	
	/**
	 *  Event content - entity. Could not be null. Events with empty content could not be processed.
	 *  
	 * @param content
	 */
	void setContent(E content);
	
	/**
	 * Persisted event content before event starts. Usable in "check modifications" processors.
	 * 
	 * @return
	 */
	E getOriginalSource();
	
	/**
	 * Persisted event content before event starts. Usable in "check modifications" processors.
	 * 
	 * @param originalSource
	 */
	void setOriginalSource(E originalSource);
	
	/**
	 * Event properties (metadata)
	 * 
	 * TODO: ConfigurationMap should be used ... see {@link Configurable}.
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
	
	
	/**
	 * Returns true, if event's type equals given eventType.
	 * 
	 * @param event
	 * @param eventType
	 * @return
	 */
	default boolean hasType(EventType eventType) {
		Assert.notNull(eventType);
		//
		return eventType.name().equals(getType().name());
	}

	/**
	 * Event class type. If is not field 'eventClassType' sets (in constructor), then will be used class from content.
	 * Processors with this generic class will be called.
	 * @return
	 */
	Class<? extends E> getEventClassType();
}
