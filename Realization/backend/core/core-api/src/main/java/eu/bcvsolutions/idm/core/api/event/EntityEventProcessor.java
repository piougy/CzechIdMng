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
	
	EntityEvent<E> process(EntityEvent<E> context);

}
