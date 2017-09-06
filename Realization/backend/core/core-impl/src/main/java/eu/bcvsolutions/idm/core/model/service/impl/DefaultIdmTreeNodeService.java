package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.processor.tree.TreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RebuildTreeNodeIndexTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Tree node service
 * - supports {@link TreeNodeEvent}
 * 
 * @author Radek Tomiška
 *
 */
@Service("treeNodeService")
public class DefaultIdmTreeNodeService 
		extends AbstractEventableDtoService<IdmTreeNodeDto, IdmTreeNode, IdmTreeNodeFilter>
		implements IdmTreeNodeService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmTreeNodeService.class);
	private final IdmTreeNodeRepository repository;
	private final IdmTreeTypeService treeTypeService;
	private final ConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;
	private final IdmIdentityContractRepository identityContractRepository;
	private final FormService formService;
	private final DefaultBaseTreeService<IdmTreeNode> baseTreeService;
	private final IdmTreeNodeForestContentService forestContentService;

	@Autowired
	public DefaultIdmTreeNodeService(
		IdmTreeNodeRepository treeNodeRepository,
		IdmTreeTypeService treeTypeService,
		ConfigurationService configurationService,
		LongRunningTaskManager longRunningTaskManager,
		FormService formService,
		EntityEventManager entityEventManager,
		IdmIdentityContractRepository identityContractRepository,
		DefaultBaseTreeService<IdmTreeNode> baseTreeService,
		IdmTreeNodeForestContentService forestContentService) {
		super(treeNodeRepository, entityEventManager);
		//
		Assert.notNull(treeTypeService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		Assert.notNull(identityContractRepository);
		Assert.notNull(formService);
		Assert.notNull(baseTreeService);
		Assert.notNull(forestContentService);
		//
		this.repository = treeNodeRepository;
		this.treeTypeService = treeTypeService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
		this.identityContractRepository = identityContractRepository;
		this.formService = formService;
		this.baseTreeService = baseTreeService;
		this.forestContentService = forestContentService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.TREENODE, getEntityClass());
	}
	
	@Override
	@Transactional
	public IdmTreeNodeDto saveInternal(IdmTreeNodeDto treeNode) {
		// if index rebuild is in progress, then throw exception
		checkTreeType(treeNode.getTreeType());
		//
		if (isNew(treeNode)) {
			this.validate(treeNode, true);
			// create new
			treeNode = super.saveInternal(treeNode);
			IdmForestIndexEntity index = forestContentService.createIndex(IdmTreeNode.toForestTreeType(treeNode.getTreeType()), treeNode.getId(), treeNode.getParent());
			return setForestIndex(treeNode, index);
		}
		this.validate(treeNode, false);	
		// update - we need to reindex first
		IdmForestIndexEntity index = forestContentService.updateIndex(IdmTreeNode.toForestTreeType(treeNode.getTreeType()), treeNode.getId(), treeNode.getParent());
		treeNode = super.saveInternal(treeNode);
		return setForestIndex(treeNode, index);
	}
	
	/**
	 * Publish {@link TreeNodeEvent} only.
	 * 
	 * @see {@link TreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional
	public void deleteInternal(IdmTreeNodeDto treeNode) {
		Assert.notNull(treeNode);
		Assert.notNull(treeNode.getTreeType());
		LOG.debug("Deleting tree node [{}] - [{}]", treeNode.getTreeType(), treeNode.getCode());
		//
		// if index rebuild is in progress, then throw exception
		checkTreeType(treeNode.getTreeType());
		//
		Page<IdmTreeNode> nodes = repository.findChildren(null, treeNode.getId(), new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeNode", treeNode.getName()));
		}		
		if (this.identityContractRepository.countByWorkPosition_Id(treeNode.getId()) > 0) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeNode", treeNode.getName()));
		}
		// TODO: eav dto
		formService.deleteValues(getRepository().findOne(treeNode.getId()));
		//
		forestContentService.deleteIndex(treeNode.getId());
		super.deleteInternal(treeNode);
	}
	
	@Override
	@Transactional
	@Deprecated
	public IdmTreeNode publishTreeNode(IdmTreeNode node, EntityEvent<IdmTreeNodeDto> event,  BasePermission... permission) {
		Assert.notNull(event, "Event must be not null!");
		Assert.notNull(node);
		event.setContent(toDto(node));
		return toEntity(this.publish(event, permission).getContent());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNodeDto> findRoots(UUID treeTypeId, Pageable pageable) {
		return toDtoPage(this.repository.findChildren(treeTypeId, null, pageable));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmTreeNodeDto> findChildrenByParent(UUID parentId, Pageable pageable) {
		return toDtoPage(this.repository.findChildren(null, parentId, pageable));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmTreeNodeDto> findAllParents(UUID treeNodeId, Sort sort) {
		return toDtos(forestContentService.findAllParents(treeNodeId, sort), true);
	}
	
	@Override
	public UUID rebuildIndexes(UUID treeTypeId) {
		Assert.notNull(treeTypeId);
		IdmTreeTypeDto treeType = treeTypeService.get(treeTypeId);
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
	protected List<Predicate> toPredicates(Root<IdmTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmTreeNodeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// fulltext
		if (!StringUtils.isEmpty(filter.getText())) {
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmTreeNode_.code)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(IdmTreeNode_.name)), "%" + filter.getText().toLowerCase() + "%")
			));
		}
		// tree type
		if (filter.getTreeTypeId() != null) {
			predicates.add(builder.equal(root.get(IdmTreeNode_.treeType).get(AbstractEntity_.id), filter.getTreeTypeId()));
		}
		// parent node
		if (filter.getTreeNode() != null) {
			// TODO: bug - forest index needs tree type - same numbers in different trees
			if (filter.isRecursively()) {
				Subquery<IdmTreeNode> subquery = query.subquery(IdmTreeNode.class);
				Root<IdmTreeNode> subRoot = subquery.from(IdmTreeNode.class);
				subquery.select(subRoot);
				subquery.where(builder.and(
					builder.equal(subRoot.get(AbstractEntity_.id), filter.getTreeNode()),
					// This is here because of the structure of forest index. We need to select only subtree and not the element itself.
					// In order to do that, we must shrink the boundaries of query so it is true only for subtree of given node.
					// Remember that between clause looks like this a >= x <= b, where a and b are boundaries, in our case lft+1 and rgt-1.
					builder.between(root.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
						builder.sum(subRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 1L),
						builder.diff(subRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt), 1L))));
				predicates.add(builder.exists(subquery));
			} else {
				predicates.add(builder.equal(root.get(IdmTreeNode_.parent).get(AbstractEntity_.id), filter.getTreeNode()));
			}
		}
		// default tree type
		if (filter.getDefaultTreeType() != null) {
			IdmTreeTypeDto defaultTreeType = treeTypeService.getDefaultTreeType();
			if (defaultTreeType == null) {
				// nothing to find
				predicates.add(builder.disjunction());
			} else {
				predicates.add(builder.equal(root.get(IdmTreeNode_.treeType).get(IdmTreeType_.id), defaultTreeType.getId()));
			}
		}

		// dyn property
		if (filter.getProperty() != null) {
			switch (filter.getProperty()) {
				case "name" :
					predicates.add(builder.equal(root.get(IdmTreeNode_.name), filter.getValue()));
					break;
				case "code" :
					predicates.add(builder.equal(root.get(IdmTreeNode_.code), filter.getValue()));
					break;
				case "externalId" :
					predicates.add(builder.equal(root.get(IdmTreeNode_.externalId), filter.getValue()));
					break;
			}
		}
		//
		return predicates;
	}
	
	private IdmTreeNodeDto setForestIndex(IdmTreeNodeDto treeNode, IdmForestIndexEntity index) {
		if (index != null) {
			treeNode.setLft(index.getLft());
			treeNode.setRgt(index.getRgt());
		}
		return treeNode;
	}
	
	private void validate(IdmTreeNodeDto node, boolean isNew) {	
		if (checkCorrectType(node, isNew)) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_TYPE,  "TreeNode ["+node.getName() +"] have bad type.");
		}
		if (this.baseTreeService.validateTreeNodeParents(isNew ? toEntity(node) : toEntity(node, repository.findOne(node.getId())))) {
			throw new TreeNodeException(CoreResultCode.TREE_NODE_BAD_PARENT,  "TreeNode ["+node.getName() +"] have bad parent.");
		}
	}
	
	/**
	 * Method check type of current node and saved node.
	 * TODO: bug - this will work only in update. If node is created, then parent from diferent type could be given
	 * 
	 * @param treeNode
	 * @return bool. True - if current and saved node is not same, false - if everything ist OK. When is node new return false;
	 */
	private boolean checkCorrectType(IdmTreeNodeDto treeNode, boolean isNew) {
		if (isNew) {
			return false;
		}
		
		IdmTreeNode currentNode = repository.findOne(treeNode.getId());
		
		if (currentNode != null) {
			return !currentNode.getTreeType().getId().equals(treeNode.getTreeType());
		} else {
			return false;
		}
	}
	
	private void checkTreeType(UUID treeTypeId) {
		IdmTreeTypeDto treeType = treeTypeService.get(treeTypeId);
		if (StringUtils.hasLength(configurationService.getValue(treeTypeService.getConfigurationPropertyName(treeType.getCode(), IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD)))) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("treeType", treeType.getCode()));
		}
	}

}
