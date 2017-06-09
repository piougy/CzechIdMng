package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Single entity event processor
 * <p>
 * Its better to use {@link Ordered} interface instead {@link Order} annotation - does not work with aspects. 
 * 
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 * @author Radek Tomiška
 * @see {@link ApplicationListener}
 * @see {@link Ordered}
 */
public interface EntityEventProcessor<E extends Serializable> extends Ordered, Configurable {
	
	@Override
	default String getConfigurableType() {
		return "processor";
	}
	
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
}
