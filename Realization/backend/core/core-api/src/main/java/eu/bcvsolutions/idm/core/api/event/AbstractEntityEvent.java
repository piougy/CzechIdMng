package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

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

	private final EventType<E> type;
	private final E content;
	private final Map<String, Serializable> properties = new LinkedHashMap<>();
	
	public AbstractEntityEvent(EventType<E> type, E content) {
		Assert.notNull(type, "Operation is required!");
		Assert.notNull(content, "Event content is required!");
		//
		this.type = type;
		this.content = content;
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
	public E getContent() {
		return content;
	}
	
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}
}
