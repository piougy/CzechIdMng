package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Deletes role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes role from repository.")
public class RoleDeleteProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final IdmRoleService service;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmRoleTreeNodeService roleTreeNodeService;
	private final IdmAuthorizationPolicyService authorizationPolicyService;
	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	private final IdmAutomaticRoleRequestService automaticRoleRequestService;
	
	@Autowired
	public RoleDeleteProcessor(
			IdmRoleService service,
			IdmIdentityRoleRepository identityRoleRepository,
			IdmConceptRoleRequestService conceptRoleRequestService,
			IdmRoleRequestService roleRequestService,
			IdmRoleTreeNodeService roleTreeNodeService,
			IdmAuthorizationPolicyService authorizationPolicyService,
			IdmAutomaticRoleAttributeService automaticRoleAttributeService,
			IdmAutomaticRoleRequestService automaticRoleRequestService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(roleRequestService);
		Assert.notNull(roleTreeNodeService);
		Assert.notNull(authorizationPolicyService);
		Assert.notNull(automaticRoleAttributeService);
		Assert.notNull(automaticRoleRequestService);
		//
		this.service = service;
		this.identityRoleRepository = identityRoleRepository;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.roleRequestService = roleRequestService;
		this.roleTreeNodeService = roleTreeNodeService;
		this.authorizationPolicyService = authorizationPolicyService;
		this.automaticRoleAttributeService = automaticRoleAttributeService;
		this.automaticRoleRequestService = automaticRoleRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto role = event.getContent();
		//
		// role assigned to identity could not be deleted
		if(identityRoleRepository.countByRole_Id(role.getId()) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", role.getName()));
		}
		//
		// automatic role attribute has assigned this role
		IdmAutomaticRoleFilter automaticRoleFilter = new IdmAutomaticRoleFilter();
		automaticRoleFilter.setRoleId(role.getId());
		long totalElements = automaticRoleAttributeService.find(automaticRoleFilter, new PageRequest(0, 1)).getTotalElements();
		if (totalElements > 0) {
			// some automatic role attribute has assigned this role
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_AUTOMATIC_ROLE_ASSIGNED, ImmutableMap.of("role", role.getName()));
		}
		//
		// remove related automatic roles
		IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
		filter.setRoleId(role.getId());
		roleTreeNodeService.find(filter, null).forEach(roleTreeNode -> {
			try {
				roleTreeNodeService.delete(roleTreeNode);
			} catch (AcceptedException ex) {
				throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_TREE_NODE, 
						ImmutableMap.of(
								"role", role.getName(),
								"roleTreeNode", roleTreeNode.getId()
								));
			}
		});
		// Find all concepts and remove relation on role
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setRoleId(role.getId());
		conceptRoleRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"Role [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						role.getName(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested role [{1}] was deleted (not from this role request)!",
						concept.getId(), role.getName());
				concept.setState(RoleRequestState.CANCELED);
			}
			roleRequestService.addToLog(request, message);
			conceptRoleRequestService.addToLog(concept, message);
			concept.setRole(null);

			roleRequestService.save(request);
			conceptRoleRequestService.save(concept);
		});
		// remove all policies
		IdmAuthorizationPolicyFilter policyFilter = new IdmAuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		authorizationPolicyService.find(policyFilter, null).forEach(dto -> {
			authorizationPolicyService.delete(dto);
		});
		// Find all automatic role requests and remove relation on automatic role
		UUID roleId = role.getId();
		if (roleId != null) {
			IdmAutomaticRoleRequestFilter automaticRoleRequestFilter = new IdmAutomaticRoleRequestFilter();
			automaticRoleRequestFilter.setRoleId(roleId);
			
			automaticRoleRequestService.find(automaticRoleRequestFilter, null).getContent().forEach(request -> {
				request.setRole(null);
				automaticRoleRequestService.save(request);
				automaticRoleRequestService.cancel(request);
			});
		}
		//		
		// remove role guarantees, sub roles and catalog works automatically by hibenate mapping
		service.deleteInternal(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}