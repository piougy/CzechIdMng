package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for tokens
 * 
 * @author Radek Tomiška
 *
 */
public class TokenEvent extends CoreEvent<IdmTokenDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 *
	 */
	public enum TokenEventType implements EventType {
		CREATE, 
		UPDATE, // prolong expiration / disable
		DELETE
	}
	
	public TokenEvent(TokenEventType operation, IdmTokenDto content) {
		super(operation, content);
	}
	
	public TokenEvent(TokenEventType operation, IdmTokenDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}