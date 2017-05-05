package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Core event processor - defines order only for now
 * 
 * @param <E> @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 * @author Radek Tomiška
 */
public abstract class CoreEventProcessor<E extends Serializable> extends AbstractEntityEventProcessor<E> {

	public CoreEventProcessor(EventType... type) {
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
