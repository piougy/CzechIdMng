package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Event to pre validate password
 * 
 * @author Patrik Stloukal
 *
 */
public class PasswordChangeEvent extends CoreEvent<PasswordChangeDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 *
	 */
	public enum PasswordChangeEventType implements EventType {

		PASSWORD_PREVALIDATION
	}
	
	public PasswordChangeEvent(PasswordChangeEventType operation, PasswordChangeDto content) {
		super(operation, content);
	}
	
	public PasswordChangeEvent(PasswordChangeEventType operation, PasswordChangeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}
