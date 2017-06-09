package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity role valid request
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityRoleValidRequestEvent extends CoreEvent<IdmIdentityRoleValidRequestDto> {

	private static final long serialVersionUID = -2854160263156190911L;
	
	public enum IdentityRoleValidRequestEventType implements EventType {
		IDENTITY_ROLE_VALID
	}
	
	public IdentityRoleValidRequestEvent(EventType type, IdmIdentityRoleValidRequestDto content,
			Map<String, Serializable> properties) {
		super(type, content, properties);
	}
	
	public IdentityRoleValidRequestEvent(EventType type, IdmIdentityRoleValidRequestDto content) {
		super(type, content);
	}
}
