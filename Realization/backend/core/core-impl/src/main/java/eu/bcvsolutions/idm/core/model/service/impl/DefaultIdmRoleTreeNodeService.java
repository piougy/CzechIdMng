package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.RoleTreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.RoleTreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic role service
 * - supports {@link RoleTreeNodeEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleTreeNodeService extends AbstractReadWriteEntityService<IdmRoleTreeNode, RoleTreeNodeFilter> implements IdmRoleTreeNodeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleTreeNodeService.class);
	private final EntityEventManager entityEventManager;
	
	public DefaultIdmRoleTreeNodeService(
			AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		//
		this.entityEventManager = entityEventManager;
	}
	
	/**
	 * Publish {@link RoleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmRoleTreeNode save(IdmRoleTreeNode roleTreeNode) {
		Assert.notNull(roleTreeNode);
		Assert.notNull(roleTreeNode.getRole());
		Assert.notNull(roleTreeNode.getTreeNode());
		//
		LOG.debug("Saving automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole().getName(), roleTreeNode.getTreeNode().getCode(), roleTreeNode.getRecursionType());
		//
		if (isNew(roleTreeNode)) { // create
			return entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.CREATE, roleTreeNode)).getContent();
		}
		return entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.UPDATE, roleTreeNode)).getContent();
	}
	
	/**
	 * Publish {@link roleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmRoleTreeNode roleTreeNode) {
		Assert.notNull(roleTreeNode);
		Assert.notNull(roleTreeNode.getRole());
		Assert.notNull(roleTreeNode.getTreeNode());
		//
		LOG.debug("Deleteing automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole().getName(), roleTreeNode.getTreeNode().getCode(), roleTreeNode.getRecursionType());
		//
		entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.DELETE, roleTreeNode));
	}

}
