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

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
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
		extends AbstractReadWriteDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNode, RoleTreeNodeFilter> 
		implements IdmRoleTreeNodeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleTreeNodeService.class);
	private final IdmRoleTreeNodeRepository repository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final EntityEventManager entityEventManager;
	private final IdmRoleRequestService roleRequestService;
	private final IdmIdentityContractService identityContractService;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	
	@Autowired
	public DefaultIdmRoleTreeNodeService(
			IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager,
			IdmRoleRequestService roleRequestService,
			IdmIdentityContractService identityContractService,
			IdmConceptRoleRequestService conceptRoleRequestService) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeNodeRepository);
		Assert.notNull(roleRequestService);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(identityContractService);
		//
		this.repository = repository;
		this.treeNodeRepository = treeNodeRepository;
		this.entityEventManager = entityEventManager;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.roleRequestService = roleRequestService;
		this.identityContractService = identityContractService;
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
	protected List<Predicate> toPredicates(Root<IdmRoleTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder, RoleTreeNodeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// 
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmRoleTreeNode_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		//
		if (filter.getTreeNodeId() != null) {
			predicates.add(builder.equal(root.get(IdmRoleTreeNode_.treeNode).get(IdmTreeNode_.id), filter.getTreeNodeId()));
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
	@Transactional
	public IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles, boolean startRequestInternal) {
		return this.processAutomaticRoles(contract, null, automaticRoles, ConceptRoleRequestOperation.ADD, startRequestInternal);
	}

	@Override
	@Transactional
	public IdmRoleRequestDto removeAutomaticRoles(IdmIdentityRoleDto identityRole,
			Set<IdmRoleTreeNodeDto> automaticRoles, boolean startRequestInternal) {
		Assert.notNull(identityRole);
		//
		IdmIdentityContractDto dto = identityContractService.get(identityRole.getIdentityContract());
		return this.processAutomaticRoles(dto, identityRole.getId(), automaticRoles, ConceptRoleRequestOperation.REMOVE, startRequestInternal);
	}
	
	private IdmRoleRequestDto processAutomaticRoles(IdmIdentityContractDto contract, UUID identityRoleId,
			Set<IdmRoleTreeNodeDto> automaticRoles, ConceptRoleRequestOperation operation,
			boolean startRequestInternal) {
		Assert.notNull(automaticRoles);
		Assert.notNull(contract);
		Assert.notNull(operation);
		//
		if (automaticRoles.isEmpty()) {
			return null;
		}
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true); // TODO: by configuration
		roleRequest = roleRequestService.save(roleRequest);
		//
		for(IdmRoleTreeNodeDto roleTreeNode : automaticRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setIdentityRole(identityRoleId);
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(roleTreeNode.getRole());
			conceptRoleRequest.setRoleTreeNode(roleTreeNode.getId());
			//
			conceptRoleRequest.setOperation(operation);
			//
			conceptRoleRequestService.save(conceptRoleRequest);
		};
		//
		if (startRequestInternal) {
			roleRequestService.startRequestInternal(roleRequest.getId(), false);
		}
		//
		return roleRequest;
	}

}
