package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleTreeNodeSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

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
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	
	public DefaultIdmRoleTreeNodeService(
			IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager,
			IdmRoleRequestService roleRequestService,
			IdmConceptRoleRequestService conceptRoleRequestService) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeNodeRepository);
		Assert.notNull(roleRequestService);
		Assert.notNull(conceptRoleRequestService);
		//
		this.repository = repository;
		this.treeNodeRepository = treeNodeRepository;
		this.entityEventManager = entityEventManager;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.roleRequestService = roleRequestService;
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
	 * Publish {@link roleTreeNodeEvent} only.
	 * 
	 * @see {@link RoleTreeNodeDeleteProcessor}
	 */
	@Override
	@Transactional(noRollbackFor = AcceptedException.class)
	public void delete(IdmRoleTreeNodeDto roleTreeNode, BasePermission... permission) {
		Assert.notNull(roleTreeNode);
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

	@Override
	@Transactional
	public IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContract contract, Set<IdmRoleTreeNode> automaticRoles, boolean startRequestInternal) {
		return this.processAutomaticRoles(contract, null, automaticRoles, ConceptRoleRequestOperation.ADD, startRequestInternal);
	}

	@Override
	@Transactional
	public IdmRoleRequestDto updateOrRemoveAutomaticRoles(IdmIdentityRole identityRole,
			Set<IdmRoleTreeNode> automaticRoles, ConceptRoleRequestOperation operation, boolean startRequestInternal) {
		Assert.notNull(identityRole);
		//
		return this.processAutomaticRoles(identityRole.getIdentityContract(), identityRole.getId(), automaticRoles, operation, startRequestInternal);
	}
	
	private IdmRoleRequestDto processAutomaticRoles(IdmIdentityContract contract, UUID identityRoleId,
			Set<IdmRoleTreeNode> automaticRoles, ConceptRoleRequestOperation operation,
			boolean startRequestInternal) {
		Assert.notNull(automaticRoles);
		Assert.notNull(contract);
		Assert.notNull(operation);
		//
		if (automaticRoles.isEmpty()) {
			return null;
		}
		
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity().getId());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true); // TODO: by configuration
		roleRequest = roleRequestService.save(roleRequest);
		//
		for(IdmRoleTreeNode roleTreeNode : automaticRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setIdentityRole(identityRoleId);
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(roleTreeNode.getRole().getId());
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
