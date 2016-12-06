package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Event state holder (content + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEvent<E extends AbstractEntity> implements EntityEvent<E> {

	private final Class<E> entityClass;
	private final String operation;
	private E content;
	private boolean complete;
	private final Map<String, Serializable> properties = new LinkedHashMap<>();
	
	@SuppressWarnings("unchecked")
	public AbstractEntityEvent(Serializable operation) {
		Assert.notNull(operation, "Operation is required!");
		Assert.hasLength(operation.toString(), "Operation is required!");
		//
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEvent.class);
		this.operation = operation.toString();
	}
	
	public AbstractEntityEvent(Serializable operation, E content) {
		this(operation);
		this.content = content;
	}
	
	public AbstractEntityEvent(Serializable operation, E content, Map<String, Serializable> properties) {
		this(operation);
		this.content = content;
		this.properties.putAll(properties);
	}

	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}

	@Override
	public String getOperation() {
		return operation;
	}

	@Override
	public E getContent() {
		return content;
	}

	@Override
	public void setContent(E content) {
		this.content = content;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}
	
	@Override
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}
}
