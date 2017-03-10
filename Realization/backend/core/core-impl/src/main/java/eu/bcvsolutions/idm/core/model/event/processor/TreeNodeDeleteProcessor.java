package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;

/**
 * Deletes tree node - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes tree node with forest index")
public class TreeNodeDeleteProcessor extends CoreEventProcessor<IdmTreeNode> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	private final IdmTreeNodeRepository repository;
	private final IdmIdentityContractRepository identityContractRepository;
	private final TreeNodeSaveProcessor saveProcessor;
	
	@Autowired
	public TreeNodeDeleteProcessor(
			IdmTreeNodeRepository repository,
			IdmIdentityContractRepository identityContractRepository,
			TreeNodeSaveProcessor saveProcessor) {
		super(TreeNodeEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(saveProcessor);
		Assert.notNull(identityContractRepository);
		//
		this.repository = repository;
		this.saveProcessor = saveProcessor;
		this.identityContractRepository = identityContractRepository;
		
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTreeNode> process(EntityEvent<IdmTreeNode> event) {
		IdmTreeNode treeNode = event.getContent();
		
		//
		// if index rebuild is in progress, then throw exception
		saveProcessor.checkTreeType(treeNode.getTreeType());
		//
		Page<IdmTreeNode> nodes = repository.findChildren(null, treeNode.getId(), new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeNode", treeNode.getName()));
		}		
		if (this.identityContractRepository.countByWorkingPosition(treeNode) > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeNode", treeNode.getName()));
		}
		// clear default tree nodes from type
		saveProcessor.getTreeTypeService().clearDefaultTreeNode(treeNode);
		//
		repository.delete(saveProcessor.getForestContentService().deleteIndex(treeNode));
		//
		return new DefaultEventResult<>(event, this);
	}
}
