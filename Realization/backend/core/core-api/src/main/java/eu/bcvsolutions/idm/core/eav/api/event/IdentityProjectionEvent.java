package eu.bcvsolutions.idm.core.eav.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for form identity projections.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class IdentityProjectionEvent extends CoreEvent<IdmIdentityProjectionDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types.
	 */
	public enum IdentityProjectionEventType implements EventType {
		CREATE, UPDATE
	}

	public IdentityProjectionEvent(IdentityProjectionEventType operation, IdmIdentityProjectionDto content) {
		super(operation, content);
	}
	
	public IdentityProjectionEvent(IdentityProjectionEventType operation, IdmIdentityProjectionDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}