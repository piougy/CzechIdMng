package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for profiles
 * 
 * @author Radek Tomiška
 *
 */
public class ProfileEvent extends CoreEvent<IdmProfileDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core events
	 *
	 */
	public enum ProfileEventType implements EventType {
		CREATE, 
		UPDATE, // prolong expiration / disable
		DELETE
	}
	
	public ProfileEvent(ProfileEventType operation, IdmProfileDto content) {
		super(operation, content);
	}
	
	public ProfileEvent(ProfileEventType operation, IdmProfileDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}