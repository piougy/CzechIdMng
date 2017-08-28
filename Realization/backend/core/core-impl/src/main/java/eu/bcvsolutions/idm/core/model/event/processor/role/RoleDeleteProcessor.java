package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Deletes role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes role from repository.")
public class RoleDeleteProcessor extends CoreEventProcessor<IdmRoleDto> {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final IdmRoleService service;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmRoleTreeNodeService roleTreeNodeService;
	private final IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Autowired
	public RoleDeleteProcessor(
			IdmRoleService service,
			IdmIdentityRoleRepository identityRoleRepository,
			IdmConceptRoleRequestService conceptRoleRequestService,
			IdmRoleRequestService roleRequestService,
			IdmRoleTreeNodeService roleTreeNodeService,
			IdmAuthorizationPolicyService authorizationPolicyService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(roleRequestService);
		Assert.notNull(roleTreeNodeService);
		Assert.notNull(authorizationPolicyService);
		//
		this.service = service;
		this.identityRoleRepository = identityRoleRepository;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.roleRequestService = roleRequestService;
		this.roleTreeNodeService = roleTreeNodeService;
		this.authorizationPolicyService = authorizationPolicyService;
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
		// remove related automatic roles
		RoleTreeNodeFilter filter = new RoleTreeNodeFilter();
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
		ConceptRoleRequestFilter conceptRequestFilter = new ConceptRoleRequestFilter();
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
		AuthorizationPolicyFilter policyFilter = new AuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		authorizationPolicyService.find(policyFilter, null).forEach(dto -> {
			authorizationPolicyService.delete(dto);
		});
		//		
		// remove role guarantees, sub roles and catalog works automatically by hibenate mapping
		service.deleteInternal(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}