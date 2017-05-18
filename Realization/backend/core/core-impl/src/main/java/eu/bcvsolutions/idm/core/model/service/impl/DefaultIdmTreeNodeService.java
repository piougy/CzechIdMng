package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.TreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.TreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RebuildTreeNodeIndexTaskExecutor;

/**
 * Tree node service
 * - supports {@link TreeNodeEvent}
 * 
 * @author Radek Tomiška
 *
 */
@Service("treeNodeService")
public class DefaultIdmTreeNodeService extends AbstractFormableService<IdmTreeNode, TreeNodeFilter>
		implements IdmTreeNodeService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmTreeNodeService.class);
	private final IdmTreeNodeRepository repository;
	private final IdmTreeTypeService treeTypeService;
	private final ConfigurationService configurationService;
	private final LongRunningTaskManager longRunningTaskManager;
	private final EntityEventManager entityEventManager;
	private final FilterManager filterManager;

	@Autowired
	public DefaultIdmTreeNodeService(
		IdmTreeNodeRepository treeNodeRepository,
		IdmTreeTypeService treeTypeService,
		ConfigurationService configurationService,
		LongRunningTaskManager longRunningTaskManager,
		FormService formService,
		EntityEventManager entityEventManager, FilterManager filterManager) {
		super(treeNodeRepository, formService);
		//
		Assert.notNull(treeTypeService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		Assert.notNull(entityEventManager);
		Assert.notNull(filterManager);
		//
		this.repository = treeNodeRepository;
		this.treeTypeService = treeTypeService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
		this.entityEventManager = entityEventManager;
		this.filterManager = filterManager;
	}
	
	/**
	 * Publish {@link TreeNodeEvent} only.
	 * 
	 * @see {@link TreeNodeSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmTreeNode save(IdmTreeNode treeNode) {
		Assert.notNull(treeNode);
		Assert.notNull(treeNode.getTreeType());
		//
		LOG.debug("Saving tree node [{}] - [{}]", treeNode.getTreeType().getCode(), treeNode.getCode());
		//
		return entityEventManager.process(new TreeNodeEvent(isNew(treeNode) ? TreeNodeEventType.CREATE : TreeNodeEventType.UPDATE, treeNode)).getContent();
	}
	
	/**
	 * Publish {@link TreeNodeEvent} only.
	 * 
	 * @see {@link TreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmTreeNode treeNode) {
		Assert.notNull(treeNode);
		Assert.notNull(treeNode.getTreeType());
		//
		LOG.debug("Deleteing tree node [{}] - [{}]", treeNode.getTreeType().getCode(), treeNode.getCode());
		entityEventManager.process(new TreeNodeEvent(TreeNodeEventType.DELETE, treeNode));
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
	@Transactional(readOnly = true)
	public Page<IdmTreeNode> find(final TreeNodeFilter filter, Pageable pageable) {
		// transform filter to criteria
		Specification<IdmTreeNode> criteria = new Specification<IdmTreeNode>() {
			public Predicate toPredicate(Root<IdmTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = DefaultIdmTreeNodeService.this.toPredicate(filter, root, query, builder);
				return query.where(predicate).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}

	private Predicate toPredicate(TreeNodeFilter filter, Root<IdmTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

		List<Predicate> predicates = new ArrayList<>();

		if (filter == null) {
			return builder.conjunction();
		}

		// fulltext
		if (!StringUtils.isEmpty(filter.getText())) {
			predicates.add(builder.or(
				builder.like(root.get(IdmTreeNode_.code), filter.getText()),
				builder.like(root.get(IdmTreeNode_.name), filter.getText())
			));
		}

		// tree type
		if (filter.getTreeTypeId() != null) {
			predicates.add(builder.equal(root.get(IdmTreeNode_.treeType).get(AbstractEntity_.id), filter.getTreeTypeId()));
		}

		// parent node
		if (filter.getTreeNode() != null) {
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
			Subquery<IdmTreeType> subQuery = query.subquery(IdmTreeType.class);
			Root<IdmTreeType> subRoot = subQuery.from(IdmTreeType.class);
			subQuery.where(
				builder.and(
					builder.equal(subRoot.get(IdmTreeType_.defaultTreeType), filter.getDefaultTreeType())),
					builder.equal(root.get(IdmTreeNode_.treeType), subRoot.getModel())
				);
			predicates.add(builder.exists(subQuery));
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

		// Dynamic filters (added, overriden by module)
		predicates.addAll(filterManager.toPredicates(root, query, builder, filter));
		//
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));

	}

}
