package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for account
 * 
 * @author Svanda
 *
 */
public class AccountEvent extends CoreEvent<AccAccountDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported identity events
	 *
	 */
	public enum AccountEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE;
	}
	
	public AccountEvent(AccountEventType operation, AccAccountDto content) {
		super(operation, content);
	}
	
	public AccountEvent(AccountEventType operation, AccAccountDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}