package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

/**
 * Empty processor - can be defined, when some result has to be added into event context outside some processor manually.
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public class EmptyEntityEventProcessor<E extends Serializable> extends AbstractEntityEventProcessor<E> {

	@Override
	public EventResult<E> process(EntityEvent<E> event) {
		return null;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
