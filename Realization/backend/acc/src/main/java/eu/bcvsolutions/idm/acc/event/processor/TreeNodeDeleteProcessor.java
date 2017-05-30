package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;

/**
 * Before tree node delete - deletes all tree node accounts
 * 
 * @author Svanda
 *
 */
@Component("accTreeNodeDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class TreeNodeDeleteProcessor extends AbstractEntityEventProcessor<IdmTreeNode> {
	
	public static final String PROCESSOR_NAME = "tree-node-delete-processor";
	private final AccTreeAccountService treeAccountService;
	
	@Autowired
	public TreeNodeDeleteProcessor(AccTreeAccountService treeAccountService) {
		super(TreeNodeEventType.DELETE);
		//
		Assert.notNull(treeAccountService);
		//
		this.treeAccountService = treeAccountService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeNode> process(EntityEvent<IdmTreeNode> event) {
		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setTreeNodeId(event.getContent().getId());
		treeAccountService.find(filter, null).forEach(treeAccount -> {
			treeAccountService.delete(treeAccount);
		});
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// right now before identity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}