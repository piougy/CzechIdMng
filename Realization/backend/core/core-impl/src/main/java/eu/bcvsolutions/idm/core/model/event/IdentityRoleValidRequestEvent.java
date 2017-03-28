package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;

/**
 * Events for identity role valid request
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityRoleValidRequestEvent extends CoreEvent<IdmIdentityRoleValidRequest> {

	private static final long serialVersionUID = -2854160263156190911L;
	
	public enum IdentityRoleValidRequestEventType implements EventType {
		IDENTITY_ROLE_VALID
	}
	
	public IdentityRoleValidRequestEvent(EventType type, IdmIdentityRoleValidRequest content,
			Map<String, Serializable> properties) {
		super(type, content, properties);
	}
	
	public IdentityRoleValidRequestEvent(EventType type, IdmIdentityRoleValidRequest content) {
		super(type, content);
	}
}
