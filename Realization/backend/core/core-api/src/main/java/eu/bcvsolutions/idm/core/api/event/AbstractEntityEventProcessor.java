package eu.bcvsolutions.idm.core.api.event;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Single entity event processor
 * 
 * Types could be {@literal null}, then processor supports all event types
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEventProcessor<E extends AbstractEntity> implements EntityEventProcessor<E> {

	private final Class<E> entityClass;
	private final EventType<E>[] types;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public AbstractEntityEventProcessor(EventType... type) {
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEventProcessor.class);
		this.types = type;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		Assert.notNull(entityEvent);
		Assert.notNull(entityEvent.getContent(), "EntityeEvent does not contain content, content is required!");
		
		return entityEvent.getContent().getClass().isAssignableFrom(entityClass)
				&& (ArrayUtils.isEmpty(types) || ArrayUtils.contains(types, entityEvent.getType()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventResult<E> process(EntityEvent<E> event, EventContext<E> context) {
		return process(event);
	}
}
