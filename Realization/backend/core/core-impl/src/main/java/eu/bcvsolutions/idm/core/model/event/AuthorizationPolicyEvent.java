package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for authorization policy
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizationPolicyEvent extends CoreEvent<IdmAuthorizationPolicyDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported policy events.
	 *
	 */
	public enum AuthorizationPolicyEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public AuthorizationPolicyEvent(EventType type, IdmAuthorizationPolicyDto content) {
		super(type, content);
	}
}
