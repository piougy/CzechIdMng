package eu.bcvsolutions.idm.core.api.event;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Single entity event processor
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public interface EntityEventProcessor<E extends AbstractEntity> extends Plugin<EntityEvent<?>> {
	
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

}
