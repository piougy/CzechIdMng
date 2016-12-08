package eu.bcvsolutions.idm.core.api.event;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Default event result holder
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public class DefaultEventResult<E extends AbstractEntity> implements EventResult<E> {
	
	private final EntityEvent<E> event;
	private final EntityEventProcessor<E> processor;
	private final boolean completed;
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor, boolean completed) {
		this.event = event;
		this.processor = processor;
		this.completed = completed;
	}
	
	public DefaultEventResult(EntityEvent<E> event, EntityEventProcessor<E> processor) {
		this(event, processor, false);
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
	public boolean isCompleted() {
		return completed;
	}
}
