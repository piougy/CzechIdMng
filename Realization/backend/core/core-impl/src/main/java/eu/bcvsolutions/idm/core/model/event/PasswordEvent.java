package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link IdmPasswordDto}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class PasswordEvent extends CoreEvent<IdmPasswordDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported event types
	 */
	public enum PasswordEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public PasswordEvent(PasswordEventType operation, IdmPasswordDto content) {
		super(operation, content);
	}
	
	public PasswordEvent(PasswordEventType operation, IdmPasswordDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
