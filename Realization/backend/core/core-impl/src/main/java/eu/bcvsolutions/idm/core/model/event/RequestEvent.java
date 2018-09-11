package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for universal requests
 * 
 * @author svandav
 *
 */
public class RequestEvent extends CoreEvent<IdmRequestDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum RequestEventType implements EventType {
		CREATE, UPDATE, DELETE, EXECUTE
	}

	public RequestEvent(RequestEventType operation, IdmRequestDto content) {
		super(operation, content);
	}

	public RequestEvent(RequestEventType operation, IdmRequestDto content,
			Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}