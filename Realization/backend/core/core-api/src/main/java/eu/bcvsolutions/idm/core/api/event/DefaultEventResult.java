package eu.bcvsolutions.idm.core.api.event;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Default event result holder
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public class DefaultEventResult<E extends BaseEntity> implements EventResult<E> {
	
	private final EntityEvent<E> event;
	private final EntityEventProcessor<E> processor;
	private boolean closed;
	private boolean suspended;
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor, boolean closed) {
		this.event = event;
		this.processor = processor;
		this.closed = closed;
	}
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor) {
		this(event, processor, false);
	}
	
	private DefaultEventResult(Builder<E> builder) {
		this(builder.event, builder.processor, builder.closed);
		this.suspended = builder.suspended;
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
		return processor.getOrder();
	}
	
	/**
	 * {@link DefaultEventResult} builder
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder<E extends BaseEntity> {
		// required
		private final EntityEvent<E> event;
		private final EntityEventProcessor<E> processor;
		// optional	
		private boolean closed;
		private boolean suspended;
		
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
		
		public DefaultEventResult<E> build() {
			return new DefaultEventResult<>(this);
		}
	}
}
