package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RebuildTreeNodeIndexTaskExecutor;

@Service
public class DefaultIdmTreeNodeService extends AbstractFormableService<IdmTreeNode, TreeNodeFilter> 
		implements IdmTreeNodeService {
	
	private final IdmTreeNodeRepository repository;
	private final IdmTreeTypeService treeTypeService;
	private final IdmIdentityContractRepository identityContractRepository;
	private final DefaultBaseTreeService<IdmTreeNode> baseTreeService;
	private final ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	private final ConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;

	@Autowired
	public DefaultIdmTreeNodeService(
			IdmTreeNodeRepository treeNodeRepository,
			IdmTreeTypeService treeTypeService,
			IdmIdentityContractRepository identityContractRepository,
			DefaultBaseTreeService<IdmTreeNode> baseTreeService,
			ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService,
			ConfigurationService configurationService,
			LongRunningTaskManager longRunningTaskManager,
			FormService formService) {
		super(treeNodeRepository, formService);
		//
		Assert.notNull(treeTypeService);
		Assert.notNull(identityContractRepository);
		Assert.notNull(baseTreeService);
		Assert.notNull(forestIndexService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		//
		this.repository = treeNodeRepository;
		this.treeTypeService = treeTypeService;
		this.identityContractRepository = identityContractRepository;
		this.baseTreeService = baseTreeService;
		this.forestIndexService = forestIndexService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	@Transactional
	public void delete(IdmTreeNode treeNode) {
		Assert.notNull(treeNode);
		//
		// if index rebuild is in progress, then throw exception
		checkTreeType(treeNode.getTreeType());
		//
		Page<IdmTreeNode> nodes = repository.findChildren(null, treeNode.getId(), new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeNode", treeNode.getName()));
		}		
		if (this.identityContractRepository.countByWorkingPosition(treeNode) > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeNode", treeNode.getName()));
		}
		// clear default tree nodes from type
		treeTypeService.clearDefaultTreeNode(treeNode);
		//
		super.delete(deleteIndex(treeNode));
	}
	
	@Override
	@Transactional
	public IdmTreeNode save(IdmTreeNode treeNode) {
		Assert.notNull(treeNode);
		//
		// if index rebuild is in progress, then throw exception
		checkTreeType(treeNode.getTreeType());
		//
		this.validate(treeNode);
		//
		if (treeNode.getId() == null) {
			// create new
			return createIndex(super.save(treeNode));
		} else {
			// update - we need to reindex first
			return super.save(updateIndex(treeNode));
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findRoots(UUID treeTypeId, Pageable pageable) {
		return this.repository.findChildren(treeTypeId, null, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findChildrenByParent(UUID parentId, Pageable pageable) {
		return this.repository.findChildren(null, parentId, pageable);
	}
	
	private void validate(IdmTreeNode node) {		
		if (this.baseTreeService.validateTreeNodeParents(node)){
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
		
		if (checkCorrectType(node)) {
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
	private boolean checkCorrectType(IdmTreeNode treeNode) {
		if (treeNode.getId() == null) {
			return false;
		}
		
		IdmTreeNode currentNode = repository.findOne(treeNode.getId());
		
		if (currentNode != null) {
			return !currentNode.getTreeType().equals(treeNode.getTreeType());
		} else {
			return false;
		}
	}
	
	private void checkTreeType(IdmTreeType treeType) {
		if (StringUtils.hasLength(configurationService.getValue(treeTypeService.getConfigurationPropertyName(treeType.getCode(), IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD)))) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("treeType", treeType.getCode()));
		}
	}

	@Override
	public void rebuildIndexes(String forestTreeType) {
		rebuildIndexes(treeTypeService.get(IdmTreeNode.toTreeTypeId(forestTreeType)));
	}
	
	@Override
	public UUID rebuildIndexes(IdmTreeType treeType) {
		Assert.notNull(treeType, "Tree type is required");
		//
		String treeTypeCode = treeType.getCode();
		RebuildTreeNodeIndexTaskExecutor rebuildTask = AutowireHelper.createBean(RebuildTreeNodeIndexTaskExecutor.class);
		rebuildTask.setTreeTypeCode(treeTypeCode);
		UUID longRunningTaskId = longRunningTaskManager.execute(rebuildTask).getExecutor().getLongRunningTaskId();
		// wee need long running task related to index rebuild immediately
		configurationService.setValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD), longRunningTaskId.toString());
		return longRunningTaskId;
	}
	
	@Override
	@Transactional
	public IdmTreeNode createIndex(IdmTreeNode content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public IdmTreeNode updateIndex(IdmTreeNode content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public IdmTreeNode deleteIndex(IdmTreeNode content) {
		return forestIndexService.dropIndex(content);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findRoots(String forestTreeType, Pageable pageable) {
		return repository.findRoots(IdmTreeNode.toTreeTypeId(forestTreeType), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findDirectChildren(IdmTreeNode parent, Pageable pageable) {
		return repository.findDirectChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> findAllChildren(IdmTreeNode parent, Pageable pageable) {
		return repository.findAllChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmTreeNode> findAllParents(IdmTreeNode content, Sort sort) {
		return repository.findAllParents(content, sort);
	}
}
