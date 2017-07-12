package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Events for tree type
 * 
 * @author Svanda
 *
 */
public class TreeTypeEvent extends CoreEvent<IdmTreeType> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum TreeTypeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public TreeTypeEvent(TreeTypeEventType operation, IdmTreeType content) {
		super(operation, content);
	}
	
	public TreeTypeEvent(TreeTypeEventType operation, IdmTreeType content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}