package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for tree type
 * 
 * @author Svanda
 *
 */
public class TreeTypeEvent extends CoreEvent<IdmTreeTypeDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum TreeTypeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public TreeTypeEvent(TreeTypeEventType operation, IdmTreeTypeDto content) {
		super(operation, content);
	}
	
	public TreeTypeEvent(TreeTypeEventType operation, IdmTreeTypeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}