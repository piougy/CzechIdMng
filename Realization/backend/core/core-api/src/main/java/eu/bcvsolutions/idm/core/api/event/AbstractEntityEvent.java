package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Event state holder (content + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEvent<E extends AbstractEntity> extends ApplicationEvent implements EntityEvent<E>, ResolvableTypeProvider {

	private static final long serialVersionUID = 2309175762418747517L;
	private final EventType<E> type;
	private final Map<String, Serializable> properties = new LinkedHashMap<>();
	
	public AbstractEntityEvent(EventType<E> type, E content) {
		super(content);
		//
		Assert.notNull(type, "Operation is required!");
		//
		this.type = type;
	}
	
	public AbstractEntityEvent(EventType<E> type, E content, Map<String, Serializable> properties) {
		this(type, content);
		this.properties.putAll(properties);
	}

	@Override
	public EventType<E> getType() {
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getContent() {
		return (E) getSource();
	}
	
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}
	
	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getContent()));
	}
}
