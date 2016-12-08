package eu.bcvsolutions.idm.core.api.event;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Single entity event processor
 * 
 * 
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEventProcessor<E extends AbstractEntity> implements EntityEventProcessor<E> {

	private final Class<E> entityClass;
	private final EventType<E> type; // TODO: array - support more operations, enum?
	
	@SuppressWarnings("unchecked")
	public AbstractEntityEventProcessor(EventType<E> type) {
		Assert.notNull(type, "Operation is required!");
		//
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEventProcessor.class);
		this.type = type;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		Assert.notNull(entityEvent);
		Assert.notNull(entityEvent.getContent(), "EntityeEvent does not contain content, content is required!");
		
		// TODO: Equals or assignable? Maybe assignable will be better ...
		// TODO: support for more operation types
		return entityEvent.getContent().getClass().equals(entityClass) 
				&& type.equals(entityEvent.getType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventResult<E> process(EntityEvent<E> event, EventContext<E> context) {
		return process(event);
	}
}
