package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Automatic role service
 * - supports {@link RoleTreeNodeEvent}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleTreeNodeService 
		extends AbstractReadWriteDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNode, IdmRoleTreeNodeFilter> 
		implements IdmRoleTreeNodeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleTreeNodeService.class);
	private final IdmRoleTreeNodeRepository repository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final EntityEventManager entityEventManager;
	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public DefaultIdmRoleTreeNodeService(
			IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager,
			IdmAutomaticRoleAttributeService automaticRoleAttributeService,
			IdmIdentityRoleService identityRoleService) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeNodeRepository);
		Assert.notNull(automaticRoleAttributeService);
		Assert.notNull(identityRoleService);
		//
		this.repository = repository;
		this.treeNodeRepository = treeNodeRepository;
		this.entityEventManager = entityEventManager;
		this.automaticRoleAttributeService = automaticRoleAttributeService;
		this.identityRoleService = identityRoleService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLETREENODE, getEntityClass());
	}
	
	/**
	 * Publish {@link RoleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeSaveProcessor}
	 */
	@Override
	@Transactional(noRollbackFor = AcceptedException.class)
	public IdmRoleTreeNodeDto save(IdmRoleTreeNodeDto roleTreeNode, BasePermission... permission) {
		Assert.notNull(roleTreeNode);
		checkAccess(toEntity(roleTreeNode, null), permission);
		//
		LOG.debug("Saving automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole(), roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		if (isNew(roleTreeNode)) { // create
			// check if exists same entity for roleId, treeNodeId and recursion type
			IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
			filter.setRoleId(roleTreeNode.getRole());
			filter.setTreeNodeId(roleTreeNode.getTreeNode());
			filter.setRecursionType(roleTreeNode.getRecursionType());
			List<IdmRoleTreeNodeDto> content = this.find(filter, null).getContent();
			if (!content.isEmpty()) {
				throw new ResultCodeException(CoreResultCode.ROLE_TREE_NODE_TYPE_EXISTS, ImmutableMap.of(
						"roleId", roleTreeNode.getRole(),
						"treeNodeId", roleTreeNode.getTreeNode(),
						"recursionType", roleTreeNode.getRecursionType()));
			}
			//
			EventContext<IdmRoleTreeNodeDto> context = entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.CREATE, roleTreeNode));
			if (context.isSuspended()) {
				throw new AcceptedException();
			}
			return context.getContent();
		}
		throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
	}
	
	/**
	 * Publish {@link RoleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional(noRollbackFor = AcceptedException.class)
	public void delete(IdmRoleTreeNodeDto roleTreeNode, BasePermission... permission) {
		Assert.notNull(roleTreeNode);
		checkAccess(this.getEntity(roleTreeNode.getId()), permission);
		//
		LOG.debug("Deleting automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole(), roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		EventContext<IdmRoleTreeNodeDto> context = entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.DELETE, roleTreeNode));
		//
		if (context.isSuspended()) {
			throw new AcceptedException();
		}
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmRoleTreeNodeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// 
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmRoleTreeNode_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		//
		if (filter.getTreeNodeId() != null) {
			predicates.add(builder.equal(root.get(IdmRoleTreeNode_.treeNode).get(IdmTreeNode_.id), filter.getTreeNodeId()));
		}
		//
		if (filter.getRecursionType() != null) {
			predicates.add(builder.equal(root.get(IdmRoleTreeNode_.recursionType), filter.getRecursionType()));
		}
		return predicates;
	}

	@Override
	@Transactional(readOnly = true)
	public Set<IdmRoleTreeNodeDto> getAutomaticRolesByTreeNode(UUID workPosition) {
		Assert.notNull(workPosition);
		// TODO: we need actual forest index - use uuid and rewrite to subquery
		IdmTreeNode treeNode = treeNodeRepository.findOne(workPosition);
		Assert.notNull(treeNode);
		//
		return new HashSet<>(toDtos(repository.findAutomaticRoles(treeNode), false));
	}

	@Override
	public IdmRoleRequestDto prepareAssignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles) {
		this.addAutomaticRoles(contract, automaticRoles);
		return null;
	}
	
	@Override
	public IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles) {
		this.addAutomaticRoles(contract, automaticRoles);
		return null;
	}

	@Override
	public IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles) {
		this.removeAutomaticRoles(identityRole, automaticRoles);
		return null;
	}

	@Override
	@Transactional
	public void addAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles) {
		// this method must has own behavior for add automatic roles,
		// method IdmAutomaticRoleAttributeService#addAutomaticRoles has annotation 
		// @Transactional with required new - this doesn't works with processor
		// IdentityContractCreateByAutomaticRoleProcessor (some test are not passed)
		// original method assignAutomaticRoles has also only @Transactional without reguired new
		for (AbstractIdmAutomaticRoleDto autoRole : automaticRoles) {
			// create identity role directly
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setRoleTreeNode(autoRole.getId());
			identityRole.setIdentityContract(contract.getId());
			identityRole.setRole(autoRole.getRole());
			identityRole.setValidFrom(contract.getValidFrom());
			identityRole.setValidTill(contract.getValidTill());
			//
			// start event with skip check authorities
			IdentityRoleEvent event = new IdentityRoleEvent(IdentityRoleEventType.CREATE, identityRole);
			event.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
			identityRoleService.publish(event);
		}
	}

	@Override
	public void removeAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles) {
		automaticRoleAttributeService.removeAutomaticRoles(identityRole);
	}
}
