package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for automatic role by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AutomaticRoleAttributeEvent extends CoreEvent<IdmAutomaticRoleAttributeDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum AutomaticRoleAttributeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType type, IdmAutomaticRoleAttributeDto content) {
		super(type, content);
	}
	
	public AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType type, IdmAutomaticRoleAttributeDto content, Map<String, Serializable> properties) {
		super(type, content, properties);
	}
	
}
