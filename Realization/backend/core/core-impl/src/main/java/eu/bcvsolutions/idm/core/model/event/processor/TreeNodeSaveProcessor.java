package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultBaseTreeService;

/**
 * Persists tree node.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists tree node with forest index")
public class TreeNodeSaveProcessor extends CoreEventProcessor<IdmTreeNode> {
	
	public static final String PROCESSOR_NAME = "tree-node-save-processor";
	private final IdmTreeNodeRepository repository;
	private final DefaultBaseTreeService<IdmTreeNode> baseTreeService;
	private final IdmTreeNodeForestContentService forestContentService;
	private final ConfigurationService configurationService;
	private final IdmTreeTypeService treeTypeService;
	
	@Autowired
	public TreeNodeSaveProcessor(
			IdmTreeNodeRepository repository,
			DefaultBaseTreeService<IdmTreeNode> baseTreeService,
			IdmTreeNodeForestContentService forestContentService,
			ConfigurationService configurationService,
			IdmTreeTypeService treeTypeService) {
		super(TreeNodeEventType.UPDATE, TreeNodeEventType.CREATE);
		//
		Assert.notNull(repository);
		Assert.notNull(baseTreeService);
		Assert.notNull(forestContentService);
		Assert.notNull(repository);
		Assert.notNull(treeTypeService);
		//
		this.repository = repository;
		this.baseTreeService = baseTreeService;
		this.forestContentService = forestContentService;
		this.configurationService = configurationService;
		this.treeTypeService = treeTypeService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeNode> process(EntityEvent<IdmTreeNode> event) {
		IdmTreeNode treeNode = event.getContent();
		boolean isNew = TreeNodeEventType.CREATE == event.getType();
		//
		//
		// if index rebuild is in progress, then throw exception
		checkTreeType(treeNode.getTreeType());
		//
		this.validate(treeNode, isNew);
		//
		if (isNew) {
			// create new
			treeNode = repository.save(treeNode);
			treeNode.setForestIndex(forestContentService.createIndex(treeNode.getForestTreeType(), treeNode.getId(), treeNode.getParentId()));
		} else {
			// update - we need to reindex first
			treeNode.setForestIndex(forestContentService.updateIndex(treeNode.getForestTreeType(), treeNode.getId(), treeNode.getParentId()));
			repository.save(treeNode);
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
	
	private void validate(IdmTreeNode node, boolean isNew) {	
		if (this.baseTreeService.validateTreeNodeParents(node)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkCorrectType(node, isNew)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_TYPE,  "TreeNode ["+node.getName() +"] have bad type.");
		}
	}
	
	/**
	 * Method check type of current node and saved node.
	 * TODO: bug - this will work only in update. If node is created, then parent from diferent type could be given
	 * 
	 * @param treeNode
	 * @return bool. True - if current and saved node is not same, false - if everything ist OK. When is node new return false;
	 */
	private boolean checkCorrectType(IdmTreeNode treeNode, boolean isNew) {
		if (isNew) {
			return false;
		}
		
		IdmTreeNode currentNode = repository.findOne(treeNode.getId());
		
		if (currentNode != null) {
			return !currentNode.getTreeType().equals(treeNode.getTreeType());
		} else {
			return false;
		}
	}
	
	protected void checkTreeType(IdmTreeType treeType) {
		if (StringUtils.hasLength(configurationService.getValue(treeTypeService.getConfigurationPropertyName(treeType.getCode(), IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD)))) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("treeType", treeType.getCode()));
		}
	}
	
	protected IdmTreeTypeService getTreeTypeService() {
		return treeTypeService;
	}
	
	protected ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> getForestContentService() {
		return forestContentService;
	}

}
