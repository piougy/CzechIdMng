package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Single entity event processor
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEventProcessor<E extends AbstractEntity> implements EntityEventProcessor<E> {

	private final Class<E> entityClass;
	private final String operation; // TODO: array - support more operations, enum?
	
	@SuppressWarnings("unchecked")
	public AbstractEntityEventProcessor(Serializable operation) {
		Assert.notNull(operation, "Operation is required!");
		Assert.hasLength(operation.toString(), "Operation is required!");
		//
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEventProcessor.class);
		this.operation = operation.toString();
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(EntityEvent<?> delimiter) {
		// TODO: Equals or assignable? Maybe assignable will be better ...
		// TODO: support for more operation types
		return delimiter.getEntityClass().equals(entityClass) 
				&& StringUtils.equals(operation, delimiter.getOperation());
	}
}
