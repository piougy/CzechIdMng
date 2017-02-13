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
	private boolean suspended;
	private Integer processedOrder;
	
	@Override
	public List<EventResult<E>> getResults() {
		return Collections.unmodifiableList(processed);
	}

	@Override
	public void addResult(EventResult<E> eventResult) {
		processed.add(eventResult);
		suspended = eventResult.isSuspended();
		processedOrder = eventResult.getProcessedOrder();
	}
	
	@Override
	public E getContent() {
		if(processed.isEmpty()) {
			return null;
		}
		return getLastResult().getEvent().getContent();
	}
	
	@Override
	public EventResult<E> getLastResult(){
		return processed.get(processed.size() - 1);
	}
	
	@Override
	public boolean isClosed() {
		if(processed.isEmpty()) {
			return false;
		}
		return getLastResult().isClosed();
	}
	
	@Override
	public boolean isSuspended() {
		return suspended;
	}
	
	@Override
	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	
	public Integer getProcessedOrder() {
		return processedOrder;
	}
	
	public void setProcessedOrder(Integer processedOrder) {
		this.processedOrder = processedOrder;
	}
}
