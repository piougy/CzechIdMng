package eu.bcvsolutions.idm.core.eav.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Events for form attributes
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class FormAttributeEvent extends CoreEvent<IdmFormAttributeDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum FormAttributeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public FormAttributeEvent(FormAttributeEventType operation, IdmFormAttributeDto content) {
		super(operation, content);
	}
	
	public FormAttributeEvent(FormAttributeEventType operation, IdmFormAttributeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}