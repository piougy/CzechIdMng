package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
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

	@Autowired
	public DefaultIdmTreeNodeService(
			IdmTreeNodeRepository treeNodeRepository,
			IdmTreeTypeService treeTypeService,
			ConfigurationService configurationService,
			LongRunningTaskManager longRunningTaskManager,
			FormService formService,
			EntityEventManager entityEventManager) {
		super(treeNodeRepository, formService);
		//
		Assert.notNull(treeTypeService);
		Assert.notNull(configurationService);
		Assert.notNull(longRunningTaskManager);
		Assert.notNull(entityEventManager);
		//
		this.repository = treeNodeRepository;
		this.treeTypeService = treeTypeService;
		this.configurationService = configurationService;
		this.longRunningTaskManager = longRunningTaskManager;
		this.entityEventManager = entityEventManager;
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
	
}
