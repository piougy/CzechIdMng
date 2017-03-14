package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.RoleTreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.RoleTreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic role service
 * - supports {@link RoleTreeNodeEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleTreeNodeService 
		extends AbstractReadWriteDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNode, RoleTreeNodeFilter> 
		implements IdmRoleTreeNodeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleTreeNodeService.class);
	private final IdmRoleTreeNodeRepository repository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final EntityEventManager entityEventManager;
	
	public DefaultIdmRoleTreeNodeService(
			IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeNodeRepository);
		//
		this.repository = repository;
		this.treeNodeRepository = treeNodeRepository;
		this.entityEventManager = entityEventManager;
	}
	
	/**
	 * Publish {@link RoleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmRoleTreeNodeDto save(IdmRoleTreeNodeDto roleTreeNode) {
		Assert.notNull(roleTreeNode);
		//
		LOG.debug("Saving automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole(), roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		if (isNew(roleTreeNode)) { // create
			return entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.CREATE, roleTreeNode)).getContent();
		}
		throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
	}
	
	/**
	 * Publish {@link roleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmRoleTreeNodeDto roleTreeNode) {
		Assert.notNull(roleTreeNode);
		//
		LOG.debug("Deleting automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole(), roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.DELETE, roleTreeNode));
	}

	@Override
	@Transactional(readOnly = true)
	public Set<IdmRoleTreeNode> getAutomaticRoles(IdmTreeNode workPosition) {
		Assert.notNull(workPosition);
		//
		Set<IdmRoleTreeNode> automaticRoles = new HashSet<>();
		//
		automaticRoles.addAll(repository.findAutomaticRoles(treeNodeRepository.findOne(workPosition.getId()))); // we need actual forest index
		// 
		return automaticRoles;
	}

}
