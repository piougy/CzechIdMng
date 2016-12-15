package eu.bcvsolutions.idm.core.api.event;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Single entity event processor
 * 
 * Its better to use {@link Ordered} interface instead {@link Order} annotation - does not work with aspects. 
 * 
 * @author Radek Tomiška
 *
 * @see {@link Plugin}
 * @see {@link Ordered}
 * @param <E> {@link AbstractEntity} type
 */
public interface EntityEventProcessor<E extends BaseEntity> extends Plugin<EntityEvent<?>>, Ordered {
	
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

}
