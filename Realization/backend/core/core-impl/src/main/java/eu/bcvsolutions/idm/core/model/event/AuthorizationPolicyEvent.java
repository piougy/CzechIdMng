package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;

public class AuthorizationPolicyEvent extends CoreEvent<IdmAuthorizationPolicy> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported policy events.
	 *
	 */
	public enum AuthorizationPolicyEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public AuthorizationPolicyEvent(EventType type, IdmAuthorizationPolicy content) {
		super(type, content);
	}
}
