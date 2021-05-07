package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
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
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveRoleCompositionTaskExecutor;

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
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleDeleteProcessor.class);
	//
	@Autowired private IdmRoleService service;
	@Autowired private LookupService lookupService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmAutomaticRoleRequestService automaticRoleRequestService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	@Autowired private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	public RoleDeleteProcessor() {
		super(RoleEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		//
		IdmRoleDto role = event.getContent();
		UUID roleId = role.getId();
		Assert.notNull(roleId, "Role id is required!");
		//
		// check role can be removed without force
		if (!forceDelete) {
			checkWithoutForceDelete(role);
		}
		//
		// Find all concepts and remove relation on role - has to be the first => concepts are created bellow 
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setRoleId(roleId);
		conceptRoleRequestService
			.find(conceptRequestFilter, null)
			.stream()
			.forEach(concept -> {
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
				conceptRoleRequestService.addToLog(concept, message);
				concept.setRole(null);
				conceptRoleRequestService.save(concept);
			});
		//
		// remove related assigned roles etc.
		if (forceDelete) {
			// remove directly assigned assigned roles (not automatic)
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setRoleId(roleId);
			identityRoleFilter.setDirectRole(Boolean.TRUE);
			identityRoleFilter.setAutomaticRole(Boolean.FALSE);
			identityRoleService
					.find(identityRoleFilter, null)
					.stream()
					.forEach(identityRole -> {
						IdmIdentityContractDto contract = lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
						UUID identityId = contract.getIdentity();
						IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
						roleRequest.setState(RoleRequestState.CONCEPT);
						roleRequest.setExecuteImmediately(true); // without approval
						roleRequest.setApplicant(identityId);
						roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
						roleRequest = roleRequestService.save(roleRequest);
						//
						IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
						conceptRoleRequest.setRoleRequest(roleRequest.getId());
						conceptRoleRequest.setIdentityRole(identityRole.getId());
						conceptRoleRequest.setRole(identityRole.getRole());
						conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
						conceptRoleRequest.setIdentityContract(contract.getId());
						conceptRoleRequest.setContractPosition(identityRole.getContractPosition());
						//
						// prevent to cancel in other concepts bellow ...
						conceptRoleRequestService.save(conceptRoleRequest);
						//
						// start event with skip check authorities
						RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
						requestEvent.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
						// set priority if given
						requestEvent.setPriority(event.getPriority());
						// set parent event (role is deleted)
						requestEvent.setParentId(event.getId());
						// prevent to start asynchronous event before previous update event is completed. 
						requestEvent.setSuperOwnerId(identityId);
						//
						roleRequestService.startRequestInternal(requestEvent);
					});
			//
			// related automatic roles by tree structure
			IdmRoleTreeNodeFilter roleTreeNodefilter = new IdmRoleTreeNodeFilter();
			roleTreeNodefilter.setRoleId(roleId);
			roleTreeNodeService
				.findIds(roleTreeNodefilter, null)
				.stream()
				.forEach(roleTreeNodeId -> {
					// sync => all asynchronous requests have to be prepared in event queue
					RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
					automaticRoleTask.setAutomaticRoleId(roleTreeNodeId);
					longRunningTaskManager.executeSync(automaticRoleTask);
				});
			//
			// related automatic roles by attribute
			IdmAutomaticRoleFilter automaticRoleFilter = new IdmAutomaticRoleFilter();
			automaticRoleFilter.setRoleId(roleId);
			automaticRoleAttributeService
				.findIds(automaticRoleFilter, null)
				.stream()
				.forEach(automaticRoleId -> {
					// sync => all asynchronous requests have to be prepared in event queue
					RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
					automaticRoleTask.setAutomaticRoleId(automaticRoleId);
					longRunningTaskManager.executeSync(automaticRoleTask);
				});
			//
			// business roles
			// prevent to cyclic composition will be processed twice (sub = superior)
			Set<UUID> processedCompositionIds = new HashSet<>();
			// by sub
			IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
			compositionFilter.setSubId(roleId);
			roleCompositionService
				.findIds(compositionFilter, null)
				.stream()
				.forEach(roleCompositionId -> {
					// sync => all asynchronous requests have to be prepared in event queue
					RemoveRoleCompositionTaskExecutor roleCompositionTask = AutowireHelper.createBean(RemoveRoleCompositionTaskExecutor.class);
					roleCompositionTask.setRoleCompositionId(roleCompositionId);
					longRunningTaskManager.executeSync(roleCompositionTask);
					//
					processedCompositionIds.add(roleCompositionTask.getRoleCompositionId());
				});
			// by superior
			compositionFilter = new IdmRoleCompositionFilter();
			compositionFilter.setSuperiorId(roleId);
			roleCompositionService
				.findIds(compositionFilter, null)
				.stream()
				.filter(roleCompositionId -> !processedCompositionIds.contains(roleCompositionId)) // ~ prevent to cyclic composition will be processed twice (sub = superior)
				.forEach(roleCompositionId -> {
					// sync => all asynchronous requests have to be prepared in event queue
					RemoveRoleCompositionTaskExecutor roleCompositionTask = AutowireHelper.createBean(RemoveRoleCompositionTaskExecutor.class);
					roleCompositionTask.setRoleCompositionId(roleCompositionId);
					longRunningTaskManager.executeSync(roleCompositionTask);
					//
					processedCompositionIds.add(roleCompositionTask.getRoleCompositionId());
				});
		}
		
		//
		// remove all policies
		IdmAuthorizationPolicyFilter policyFilter = new IdmAuthorizationPolicyFilter();
		policyFilter.setRoleId(roleId);
		authorizationPolicyService.find(policyFilter, null).forEach(dto -> {
			authorizationPolicyService.delete(dto);
		});
		//
		// Find all automatic role requests and remove relation on automatic role
		IdmAutomaticRoleRequestFilter automaticRoleRequestFilter = new IdmAutomaticRoleRequestFilter();
		automaticRoleRequestFilter.setRoleId(roleId);
		
		automaticRoleRequestService.find(automaticRoleRequestFilter, null).getContent().forEach(request -> {
			request.setRole(null);
			automaticRoleRequestService.save(request);
			automaticRoleRequestService.cancel(request);
		});
		//
		// remove role guarantee
		IdmRoleGuaranteeRoleFilter roleGuaranteeRoleFilter = new IdmRoleGuaranteeRoleFilter();
		roleGuaranteeRoleFilter.setGuaranteeRole(roleId);
		roleGuaranteeRoleService.find(roleGuaranteeRoleFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeRoleService.delete(roleGuarantee);
		});
		roleGuaranteeRoleFilter = new IdmRoleGuaranteeRoleFilter();
		roleGuaranteeRoleFilter.setRole(roleId);
		roleGuaranteeRoleService.find(roleGuaranteeRoleFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeRoleService.delete(roleGuarantee);
		});
		//
		// remove guarantees
		IdmRoleGuaranteeFilter roleGuaranteeFilter = new IdmRoleGuaranteeFilter();
		roleGuaranteeFilter.setRole(roleId);
		roleGuaranteeService.find(roleGuaranteeFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeService.delete(roleGuarantee);
		});
		//
		// remove catalogues
		IdmRoleCatalogueRoleFilter roleCatalogueRoleFilter = new IdmRoleCatalogueRoleFilter();
		roleCatalogueRoleFilter.setRoleId(roleId);
		roleCatalogueRoleService.find(roleCatalogueRoleFilter, null).forEach(roleCatalogue -> {
			roleCatalogueRoleService.delete(roleCatalogue);
		});
		//
		// remove incompatible roles from both sides
		incompatibleRoleService.findAllByRole(roleId).forEach(incompatibleRole -> {
			incompatibleRoleService.delete(incompatibleRole);
		});
		//
		// Remove role-form-attributes
		IdmRoleFormAttributeFilter roleFormAttributeFilter = new IdmRoleFormAttributeFilter();
		roleFormAttributeFilter.setRole(roleId);
		roleFormAttributeService.find(roleFormAttributeFilter, null).forEach(roleCatalogue -> {
			roleFormAttributeService.delete(roleCatalogue);
		});
		//
		if (forceDelete) {
			LOG.debug("Role [{}] should be deleted by caller after all asynchronus processes are completed.", role.getCode());
		} else {
			service.deleteInternal(role);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Check role can be deleted without force delete.
	 * 
	 * @param role deleted role
	 * @throws ResultCodeException if not
	 */
	private void checkWithoutForceDelete(IdmRoleDto role) {
		UUID roleId = role.getId();
		// check assigned roles
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setRoleId(roleId);
		if (identityRoleService.count(identityRoleFilter) > 0) {		
			throw new ResultCodeException(
					CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, 
					ImmutableMap.of("role", role.getCode())
			);
		}
		//
		// automatic roles by tree structure
		IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
		filter.setRoleId(roleId);
		if (roleTreeNodeService.count(filter) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_TREE_NODE, 
					ImmutableMap.of("role", role.getCode()));
		}
		//
		// related automatic roles by attribute
		IdmAutomaticRoleFilter automaticRoleFilter = new IdmAutomaticRoleFilter();
		automaticRoleFilter.setRoleId(roleId);
		if (automaticRoleAttributeService.count(automaticRoleFilter) > 0) {
			// some automatic role attribute has assigned this role
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_AUTOMATIC_ROLE_ASSIGNED, ImmutableMap.of("role", role.getCode()));
		}
		//
		// business roles
		IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSubId(roleId);
		if (roleCompositionService.count(compositionFilter) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_COMPOSITION, 
					ImmutableMap.of("role", role.getCode()));
		}
		compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSuperiorId(roleId);
		if (roleCompositionService.count(compositionFilter) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_HAS_COMPOSITION, 
					ImmutableMap.of("role", role.getCode()));
		}
	}
}
