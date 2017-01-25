package eu.bcvsolutions.idm.core.api.event;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Single entity event processor
 * 
 * Its better to use {@link Ordered} interface instead {@link Order} annotation - does not work with aspects. 
 * 
 * @author Radek Tomiška
 *
 * @see {@link ApplicationListener}
 * @see {@link Ordered}
 * @param <E> {@link BaseEntity} type
 */
public interface EntityEventProcessor<E extends BaseEntity> extends Ordered {
	
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
	 * Process entity event with context.
	 * 
	 * @param event
	 * @param context
	 * @return
	 */
	EventResult<E> process(EntityEvent<E> event, EventContext<E> context);
	
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

}
