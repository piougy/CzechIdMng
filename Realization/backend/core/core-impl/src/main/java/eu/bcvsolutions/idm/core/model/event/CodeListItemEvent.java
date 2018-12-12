package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;

/**
 * Events for code list items
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class CodeListItemEvent extends CoreEvent<IdmCodeListItemDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 *
	 */
	public enum CodeListItemEventType implements EventType {
		CREATE, 
		UPDATE,
		DELETE
	}
	
	public CodeListItemEvent(CodeListItemEventType operation, IdmCodeListItemDto content) {
		super(operation, content);
	}
	
	public CodeListItemEvent(CodeListItemEventType operation, IdmCodeListItemDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}