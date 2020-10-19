package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
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
		extends AbstractEventableDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNode, IdmRoleTreeNodeFilter> 
		implements IdmRoleTreeNodeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleTreeNodeService.class);
	private final IdmRoleTreeNodeRepository repository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final EntityEventManager entityEventManager;
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public DefaultIdmRoleTreeNodeService(
			IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager,
			IdmIdentityRoleService identityRoleService) {
		super(repository, entityEventManager);
		//
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(treeNodeRepository, "Repository is required.");
		Assert.notNull(identityRoleService, "Service is required.");
		//
		this.repository = repository;
		this.treeNodeRepository = treeNodeRepository;
		this.entityEventManager = entityEventManager;
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
		Assert.notNull(roleTreeNode, "Automatic role is required.");
		checkAccess(toEntity(roleTreeNode, null), permission);
		//
		LOG.debug("Saving automatic role [{}] - [{}] - [{}]", roleTreeNode.getRole(), roleTreeNode.getTreeNode(), roleTreeNode.getRecursionType());
		//
		// FIXME: this should be in save processor ... can be skipped by publishing raw create event
		if (isNew(roleTreeNode)) { // create
			EventContext<IdmRoleTreeNodeDto> context = entityEventManager.process(new RoleTreeNodeEvent(RoleTreeNodeEventType.CREATE, roleTreeNode));
			if (context.isSuspended()) {
				throw new AcceptedException(String.valueOf(context.getContent().getId()));
			}
			return context.getContent();
		}
		throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmRoleTreeNodeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// 
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmRoleTreeNode_.name)), "%" + text + "%"),
							builder.like(builder.lower(root.get(IdmRoleTreeNode_.role).get(IdmRole_.name)), "%" + text + "%"),
							builder.like(builder.lower(root.get(IdmRoleTreeNode_.role).get(IdmRole_.code)), "%" + text + "%")
							));
		}
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
		Assert.notNull(workPosition, "Tree node (work position) is required.");
		// TODO: we need actual forest index - use uuid and rewrite to subquery
		IdmTreeNode treeNode = treeNodeRepository.findById(workPosition).get();
		//
		return new HashSet<>(toDtos(repository.findAutomaticRoles(treeNode), false));
	}

	@Override
	@Transactional
	public void addAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles) {
		// this method must has own behavior for add automatic roles,
		// method IdmAutomaticRoleAttributeService#addAutomaticRoles has annotation 
		// @Transactional with required new - this doesn't works with processor
		// IdentityContractCreateByAutomaticRoleProcessor (some test are not passed)
		// original method assignAutomaticRoles has also only @Transactional without reguired new
		createIdentityRole(contract, null, automaticRoles);
	}
	
	@Override
	public void addAutomaticRoles(IdmContractPositionDto contractPosition, Set<IdmRoleTreeNodeDto> automaticRoles) {
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(contractPosition, IdmContractPosition_.identityContract);
		createIdentityRole(contract, contractPosition, automaticRoles);
	}

	@Override
	public void removeAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles) {
		IdentityRoleEvent event = new IdentityRoleEvent(IdentityRoleEventType.DELETE, identityRole);
		event.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
		identityRoleService.publish(event);
	}

	/**
	 * Method create identity role and start event with create
	 * the identity role and skip check authorities.
	 *
	 * @param contract
	 * @param automaticRoles
	 */
	private void createIdentityRole(IdmIdentityContractDto contract, IdmContractPositionDto contractPosition, Set<IdmRoleTreeNodeDto> automaticRoles) {
		for (AbstractIdmAutomaticRoleDto autoRole : automaticRoles) {
			// create identity role directly
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setAutomaticRole(autoRole.getId());
			identityRole.setIdentityContract(contract.getId());
			identityRole.setContractPosition(contractPosition == null ? null : contractPosition.getId());
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
}
