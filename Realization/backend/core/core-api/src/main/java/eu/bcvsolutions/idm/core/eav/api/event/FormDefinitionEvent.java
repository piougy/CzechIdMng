package eu.bcvsolutions.idm.core.eav.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Events for form definitions
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class FormDefinitionEvent extends CoreEvent<IdmFormDefinitionDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum FormDefinitionEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public FormDefinitionEvent(FormDefinitionEventType operation, IdmFormDefinitionDto content) {
		super(operation, content);
	}
	
	public FormDefinitionEvent(FormDefinitionEventType operation, IdmFormDefinitionDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}