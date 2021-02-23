package eu.bcvsolutions.idm.core.scheduler.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Events for LRT.
 * 
 * @author Radek Tomiška
 * 
 */
public class LongRunningTaskEvent extends CoreEvent<IdmLongRunningTaskDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum LongRunningTaskEventType implements EventType {
		CREATE,
		UPDATE,
		DELETE,
		START, // @since 10.8.0 - running entity event is visible, when LRT is running / waiting.
		END // LRT ends (called after START event is completed)
	}
	
	public LongRunningTaskEvent(LongRunningTaskEventType operation, IdmLongRunningTaskDto content) {
		super(operation, content);
	}
	
	public LongRunningTaskEvent(LongRunningTaskEventType operation, IdmLongRunningTaskDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}