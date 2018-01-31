package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events type for {@Link IdmAutomaticRoleAttributeRuleDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AutomaticRoleAttributeRuleEvent extends CoreEvent<IdmAutomaticRoleAttributeRuleDto> {

	private static final long serialVersionUID = 1L;

	public enum AutomaticRoleAttributeRuleEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType operation, IdmAutomaticRoleAttributeRuleDto content) {
		super(operation, content);
	}
	
	public AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType operation, IdmAutomaticRoleAttributeRuleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
