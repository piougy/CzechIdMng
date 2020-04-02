package eu.bcvsolutions.idm.core.eav.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;

/**
 * Events for form projections.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class FormProjectionEvent extends CoreEvent<IdmFormProjectionDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types.
	 */
	public enum FormProjectionEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public FormProjectionEvent(FormProjectionEventType operation, IdmFormProjectionDto content) {
		super(operation, content);
	}
	
	public FormProjectionEvent(FormProjectionEventType operation, IdmFormProjectionDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}