package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Password policy event
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class PasswordPolicyEvent extends CoreEvent<IdmPasswordPolicyDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum PasswordPolicyEvenType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public PasswordPolicyEvent(PasswordPolicyEvenType operation, IdmPasswordPolicyDto content) {
		super(operation, content);
	}
	
	public PasswordPolicyEvent(PasswordPolicyEvenType operation, IdmPasswordPolicyDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
