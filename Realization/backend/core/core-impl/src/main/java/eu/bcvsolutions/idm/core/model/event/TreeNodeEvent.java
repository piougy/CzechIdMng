package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for tree node
 * 
 * @author Radek Tomiška
 *
 */
public class TreeNodeEvent extends CoreEvent<IdmTreeNodeDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum TreeNodeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public TreeNodeEvent(TreeNodeEventType operation, IdmTreeNodeDto content) {
		super(operation, content);
	}
	
	public TreeNodeEvent(TreeNodeEventType operation, IdmTreeNodeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}