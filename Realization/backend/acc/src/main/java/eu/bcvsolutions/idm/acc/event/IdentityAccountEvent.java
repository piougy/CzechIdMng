package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity account
 * 
 * @author Svanda
 *
 */
public class IdentityAccountEvent extends CoreEvent<AccIdentityAccountDto> {

	private static final long serialVersionUID = 1L;
static final int DEFAULT_PROVISIONING_ORDER = 1000;
	
	static final int DEFAULT_PASSWORD_VALIDATION_ORDER = -1000;
	
	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityAccountEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE;
	}
	
	public IdentityAccountEvent(IdentityAccountEventType operation, AccIdentityAccountDto content) {
		super(operation, content);
	}
	
	public IdentityAccountEvent(IdentityAccountEventType operation, AccIdentityAccountDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}