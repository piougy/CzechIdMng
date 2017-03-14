package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Single entity event processor
 * 
 * Its better to use {@link Ordered} interface instead {@link Order} annotation - does not work with aspects. 
 * 
 * @author Radek Tomiška
 *
 * @see {@link ApplicationListener}
 * @see {@link Ordered}
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public interface EntityEventProcessor<E extends Serializable> extends Ordered {
	
	static final String PROPERTY_ORDER = "order";
	static final String PROPERTY_ENABLED = "enabled";
	
	/**
	 * Unique (module scope) entity event processor identifier. Could be used in configuration etc.
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Module identifier
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Returns entity class, which supports this processor
	 * 
	 * @return
	 */
	Class<E> getEntityClass();
	
	/**
	 * Returns event types, which supports this processor
	 * 
	 * @return
	 */
	String[] getEventTypes();
	
	/**
	 * Returns true, when processor supports given event
	 * 
	 * @param entityEvent
	 * @return
	 */
	boolean supports(EntityEvent<?> entityEvent);
	
	/**
	 * Process entity event without context.
	 * 
	 * @param event
	 * @return
	 */
	EventResult<E> process(EntityEvent<E> event);
	
	/**
	 * Returns true, when processor could close event (only documentatio purpose now)
	 * 
	 * @return
	 */
	boolean isClosable();
	
	/**
	 * Returns true, when processor could be disabled
	 * 
	 * @return
	 */
	boolean isDisableable();
	
	/**
	 * Returns true, when processor is disabled
	 * 
	 * @return
	 */
	boolean isDisabled();
	
	/**
	 * Returns ccnfiguration property names for this processor
	 * 
	 * @return
	 */
	List<String> getPropertyNames();
	
	/**
	 * Returns configuration properties for this processor (all properties by configuration prefix)
	 * 
	 * @see {@link #getConfigurationPrefix()}
	 * @see {@link #getPropertyNames()}
	 * @see {@link ConfigurationService}
	 * 
	 * @return
	 */
	Map<String, String> getConfigurationProperties();

}
