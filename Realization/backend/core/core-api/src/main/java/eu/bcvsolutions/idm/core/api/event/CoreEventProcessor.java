package eu.bcvsolutions.idm.core.api.event;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Core event processor - defines order only for now
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class CoreEventProcessor<E extends AbstractEntity> extends AbstractEntityEventProcessor<E> {

	public CoreEventProcessor(EventType<E> type) {
		super(type);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}
