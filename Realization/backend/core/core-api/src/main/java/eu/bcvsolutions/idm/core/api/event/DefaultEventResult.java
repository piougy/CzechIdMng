package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Default event result holder
 * 
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 * @author Radek Tomiška
 */
public class DefaultEventResult<E extends Serializable> implements EventResult<E> {
	
	private static final long serialVersionUID = 1749982602265978363L;
	private final EntityEvent<E> event;
	@JsonIgnore
	private transient EntityEventProcessor<E> processor;
	private boolean closed;
	private boolean suspended;
	private int processedOrder;
	private List<OperationResult> results;
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor, boolean closed) {
		Assert.notNull(event);
		Assert.notNull(processor);
		//
		// TODO: clone event by clone constructor - mutable previous event content :/
		this.event = event;
		this.processor = processor;
		this.processedOrder = processor.getOrder();
		this.closed = closed;
	}
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor) {
		this(event, processor, false);
	}
	
	protected DefaultEventResult(Builder<E> builder) {
		this(builder.event, builder.processor, builder.closed);
		this.suspended = builder.suspended;
		this.results = builder.results;
	}

	@Override
	public EntityEvent<E> getEvent() {
		return this.event;
	}

	@Override
	public EntityEventProcessor<E> getProcessor() {
		return this.processor;
	}
	
	@Override
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public boolean isSuspended() {
		return suspended;
	}
	
	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	
	@Override
	public int getProcessedOrder() {
		return processedOrder;
	}
	
	@Override
	public List<OperationResult> getResults() {
		if (results == null) {
			results = new ArrayList<>();
		}
		return results;
	}
	
	public void setResults(List<OperationResult> results) {
		this.results = results;
	}
	
	/**
	 * {@link DefaultEventResult} builder
	 * 
	 * @author Radek Tomiška
	 */
	public static class Builder<E extends Serializable> {
		// required
		private final EntityEvent<E> event;
		private final EntityEventProcessor<E> processor;
		// optional	
		private boolean closed;
		private boolean suspended;
		private List<OperationResult> results;
		
		public Builder(EntityEvent<E> event, EntityEventProcessor<E> processor) {
			this.event = event;
			this.processor = processor;
		}

		public Builder<E> setClosed(boolean closed) {
			this.closed = closed;
			return this;
		}
		
		public Builder<E> setSuspended(boolean suspended) {
			this.suspended = suspended;
			return this;
		}
		
		public Builder<E> setResult(OperationResult result) {
			return setResults(result == null ? null : Lists.newArrayList(result));
		}
		
		public Builder<E> setResults(List<OperationResult> results) {
			this.results = results;
			return this;
		}
		
		public DefaultEventResult<E> build() {
			return new DefaultEventResult<>(this);
		}
		
		
	}
}
