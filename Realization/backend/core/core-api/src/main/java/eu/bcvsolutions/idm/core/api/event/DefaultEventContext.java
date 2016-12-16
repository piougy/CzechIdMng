package eu.bcvsolutions.idm.core.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Default event context state holder (event results + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public class DefaultEventContext<E extends BaseEntity> implements EventContext<E> {

	private final List<EventResult<E>> processed = new ArrayList<>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EventResult<E>> getResults() {
		return Collections.unmodifiableList(processed);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addResult(EventResult<E> eventResult) {
		processed.add(eventResult);
	}
	
	/**
	 * Returns last event content, or null, if no event was processed.
	 * 
	 * @return
	 */
	@Override
	public E getContent() {
		if(processed.isEmpty()) {
			return null;
		}
		return processed.get(processed.size() - 1).getEvent().getContent();
	}
	
	/**
	 * Event is closed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	@Override
	public boolean isClosed() {
		if(processed.isEmpty()) {
			return false;
		}
		return processed.get(processed.size() - 1).isClosed();
	}
}
