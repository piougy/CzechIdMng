package eu.bcvsolutions.idm.core.model.event.processor.tree;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;

/**
 * Publish tree node change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish tree node change event.")
public class TreeNodePublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmTreeNodeDto> {

	public static final String PROCESSOR_NAME = "tree-node-publish-change-processor";
	
	public TreeNodePublishChangeProcessor() {
		super(TreeNodeEventType.CREATE, TreeNodeEventType.UPDATE, TreeNodeEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
