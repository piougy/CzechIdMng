package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;

/**
 * Events for entity states
 * 
 * @author Radek Tomiška
 *
 */
public class EntityStateEvent extends CoreEvent<IdmEntityStateDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum EntityStateEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public EntityStateEvent(EntityStateEventType operation, IdmEntityStateDto content) {
		super(operation, content);
	}
	
	public EntityStateEvent(EntityStateEventType operation, IdmEntityStateDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}