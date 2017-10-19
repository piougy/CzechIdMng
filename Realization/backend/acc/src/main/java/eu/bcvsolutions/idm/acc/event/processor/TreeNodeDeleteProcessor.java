package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.AccTreeAccountFilter;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;

/**
 * Before tree node delete - deletes all tree node accounts
 * 
 * @author Svanda
 *
 */
@Component("accTreeNodeDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class TreeNodeDeleteProcessor extends AbstractEntityEventProcessor<IdmTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "tree-node-delete-processor";
	private final AccTreeAccountService treeAccountService;
	private final SysSyncConfigRepository syncConfigRepository;
	
	@Autowired
	public TreeNodeDeleteProcessor(AccTreeAccountService treeAccountService,
			SysSyncConfigRepository syncConfigRepository) {
		super(TreeNodeEventType.DELETE);
		//
		Assert.notNull(treeAccountService);
		Assert.notNull(syncConfigRepository);
		//
		this.treeAccountService = treeAccountService;
		this.syncConfigRepository = syncConfigRepository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		IdmTreeNodeDto node = event.getContent();
		AccTreeAccountFilter filter = new AccTreeAccountFilter();
		filter.setTreeNodeId(node.getId());
		treeAccountService.find(filter, null).forEach(treeAccount -> {
			treeAccountService.delete(treeAccount);
		});
		
		// Delete link to sync contract configuration
		if(node != null && node.getId() != null) {
			syncConfigRepository.clearDefaultTreeNode(node.getId());
		}
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