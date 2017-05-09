package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityEvent extends CoreEvent<IdmIdentityDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityEventType implements EventType {
		CREATE, UPDATE, DELETE, PASSWORD
	}
	
	public IdentityEvent(IdentityEventType operation, IdmIdentityDto content) {
		super(operation, content);
	}
	
	public IdentityEvent(IdentityEventType operation, IdmIdentityDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}