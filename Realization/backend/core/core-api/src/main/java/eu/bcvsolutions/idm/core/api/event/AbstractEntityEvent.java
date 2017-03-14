package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Event state holder (content + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public abstract class AbstractEntityEvent<E extends Serializable> extends ApplicationEvent implements EntityEvent<E> {

	private static final long serialVersionUID = 2309175762418747517L;
	private E content; // mutable content - is changed during processing
	private final EventType type;
	private final Map<String, Serializable> properties = new LinkedHashMap<>();
	@JsonIgnore
	private final EventContext<E> context;
	
	public AbstractEntityEvent(EventType type, E content, Map<String, Serializable> properties, EventContext<E> context) {
		super(content);
		//
		Assert.notNull(type, "Operation is required!");
		//
		this.content = content;
		this.type = type;
		if (properties != null) {
			this.properties.putAll(properties);
		}
		this.context = context == null ? new DefaultEventContext<>() : context;
	}
	
	public AbstractEntityEvent(EventType type, E content, Map<String, Serializable> properties) {
		this(type, content, properties, null);
	}

	public AbstractEntityEvent(EventType type, E content) {
		this(type, content, null, null);
	}

	@Override
	public EventType getType() {
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getSource() {
		return (E) super.getSource();
	}
	
	@Override
	public E getContent() {
		return content;
	}
	
	@Override
	public void setContent(E content) {
		Assert.notNull(content, "Content is required!");
		//
		this.content = content;
	}	
	
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}
	
	@Override
	public EventContext<E> getContext() {
		return context;
	}
	
	@Override
	public boolean isClosed() {
		return context.isClosed();
	}
	
	@Override
	public boolean isSuspended() {
		return context.isSuspended();
	}
	
	@Override
	public Integer getProcessedOrder() {
		return context.getProcessedOrder();
	}
	
	@Override
	@JsonIgnore
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass().getSuperclass(), ResolvableType.forInstance(getContent()));
    }
}
