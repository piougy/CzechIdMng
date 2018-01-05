package eu.bcvsolutions.idm.core.scheduler.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Events for LRT
 * 
 * @author Radek Tomiška
 * @deprecated use {@link eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent}
 */
@Deprecated
public class LongRunningTaskEvent extends CoreEvent<IdmLongRunningTaskDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 * 
	 * use {@link eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType}
	 */
	@Deprecated
	public enum LongRunningTaskEventType implements EventType {
		END
	}
	
	public LongRunningTaskEvent(LongRunningTaskEventType operation, IdmLongRunningTaskDto content) {
		super(operation, content);
	}
	
	public LongRunningTaskEvent(LongRunningTaskEventType operation, IdmLongRunningTaskDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}