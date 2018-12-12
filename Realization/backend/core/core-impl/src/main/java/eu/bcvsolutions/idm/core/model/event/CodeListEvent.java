package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;

/**
 * Events for code lists
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class CodeListEvent extends CoreEvent<IdmCodeListDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 *
	 */
	public enum CodeListEventType implements EventType {
		CREATE, 
		UPDATE,
		DELETE
	}
	
	public CodeListEvent(CodeListEventType operation, IdmCodeListDto content) {
		super(operation, content);
	}
	
	public CodeListEvent(CodeListEventType operation, IdmCodeListDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}