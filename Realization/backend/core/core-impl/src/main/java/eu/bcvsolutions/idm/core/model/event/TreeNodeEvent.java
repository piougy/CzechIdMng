package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Events for tree node
 * 
 * @author Radek Tomiška
 *
 */
public class TreeNodeEvent extends CoreEvent<IdmTreeNode> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum TreeNodeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public TreeNodeEvent(TreeNodeEventType operation, IdmTreeNode content) {
		super(operation, content);
	}
	
	public TreeNodeEvent(TreeNodeEventType operation, IdmTreeNode content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}