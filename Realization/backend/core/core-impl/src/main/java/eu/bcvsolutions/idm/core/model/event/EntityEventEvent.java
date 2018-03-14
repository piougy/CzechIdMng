package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for entity events. Yes, event has event :)
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventEvent extends CoreEvent<IdmEntityEventDto> {
	
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_ORDER = 10000;
	
	/**
	 * Supported event types
	 */
	public enum EntityEventType implements EventType {
		CREATE, UPDATE, DELETE, EXECUTE
	}

	public EntityEventEvent(EntityEventType operation, IdmEntityEventDto content) {
		super(operation, content);
	}
	
	public EntityEventEvent(EntityEventType operation, IdmEntityEventDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}