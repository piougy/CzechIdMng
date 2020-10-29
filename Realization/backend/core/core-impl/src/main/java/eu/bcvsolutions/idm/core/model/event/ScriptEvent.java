package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for scripts.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public class ScriptEvent extends CoreEvent<IdmScriptDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core event types.
	 *
	 */
	public enum ScriptEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public ScriptEvent(ScriptEventType operation, IdmScriptDto content) {
		super(operation, content);
	}

	public ScriptEvent(ScriptEventType operation, IdmScriptDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}