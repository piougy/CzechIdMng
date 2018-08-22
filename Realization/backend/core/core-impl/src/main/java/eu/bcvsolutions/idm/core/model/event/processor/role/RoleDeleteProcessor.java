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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
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
@Component(RoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes role from repository.")
public class RoleDeleteProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	//
	@Autowired private IdmRoleService service;
	@Autowired private IdmIdentityRoleRepository identityRoleRepository;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmAutomaticRoleRequestService automaticRoleRequestService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	@Autowired private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	
	public RoleDeleteProcessor() {
		super(RoleEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto role = event.getContent();
		Assert.notNull(role.getId(), "Role id is required!");
		//
		// role assigned to identity could not be deleted
		if(identityRoleRepository.countByRole_Id(role.getId()) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", role.getCode()));
		}
		//
		// automatic role attribute has assigned this role
		IdmAutomaticRoleFilter automaticRoleFilter = new IdmAutomaticRoleFilter();
		automaticRoleFilter.setRoleId(role.getId());
		long totalElements = automaticRoleAttributeService.find(automaticRoleFilter, new PageRequest(0, 1)).getTotalElements();
		if (totalElements > 0) {
			// some automatic role attribute has assigned this role
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_AUTOMATIC_ROLE_ASSIGNED, ImmutableMap.of("role", role.getCode()));
		}
		//
		// related automatic roles
		IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
		filter.setRoleId(role.getId());
		if (roleTreeNodeService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_TREE_NODE, 
					ImmutableMap.of("role", role.getCode()));
		}
		//
		// business roles
		IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSubId(role.getId());
		if (roleCompositionService.find(compositionFilter, new PageRequest(0, 1)).getTotalElements() > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_COMPOSITION, 
					ImmutableMap.of("role", role.getCode()));
		}
		compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSuperiorId(role.getId());
		if (roleCompositionService.find(compositionFilter, new PageRequest(0, 1)).getTotalElements() > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_COMPOSITION, 
					ImmutableMap.of("role", role.getCode()));
		}
		//
		// Find all concepts and remove relation on role
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setRoleId(role.getId());
		conceptRoleRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"Role [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						role.getCode(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested role [{1}] was deleted (not from this role request)!",
						concept.getId(), role.getCode());
				// Cancel concept and WF
				concept = conceptRoleRequestService.cancel(concept);
			}
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
			roleRequestService.addToLog(request, message);
			conceptRoleRequestService.addToLog(concept, message);
			concept.setRole(null);

			roleRequestService.save(request);
			conceptRoleRequestService.save(concept);
		});
		//
		// remove all policies
		IdmAuthorizationPolicyFilter policyFilter = new IdmAuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		authorizationPolicyService.find(policyFilter, null).forEach(dto -> {
			authorizationPolicyService.delete(dto);
		});
		//
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
		// remove role guarantee
		IdmRoleGuaranteeRoleFilter roleGuaranteeRoleFilter = new IdmRoleGuaranteeRoleFilter();
		roleGuaranteeRoleFilter.setGuaranteeRole(role.getId());
		roleGuaranteeRoleService.find(roleGuaranteeRoleFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeRoleService.delete(roleGuarantee);
		});
		roleGuaranteeRoleFilter = new IdmRoleGuaranteeRoleFilter();
		roleGuaranteeRoleFilter.setRole(role.getId());
		roleGuaranteeRoleService.find(roleGuaranteeRoleFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeRoleService.delete(roleGuarantee);
		});
		//
		// remove guarantees
		IdmRoleGuaranteeFilter roleGuaranteeFilter = new IdmRoleGuaranteeFilter();
		roleGuaranteeFilter.setRole(role.getId());
		roleGuaranteeService.find(roleGuaranteeFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeService.delete(roleGuarantee);
		});
		//
		// remove catalogues
		IdmRoleCatalogueRoleFilter roleCatalogueRoleFilter = new IdmRoleCatalogueRoleFilter();
		roleCatalogueRoleFilter.setRoleId(role.getId());
		roleCatalogueRoleService.find(roleCatalogueRoleFilter, null).forEach(roleCatalogue -> {
			roleCatalogueRoleService.delete(roleCatalogue);
		});
		//		
		// TODO: role composition
		//
		service.deleteInternal(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}