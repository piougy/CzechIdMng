package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Delete identity - ensures referential integrity.
 * 
 * @author Radek Tomiška
 */
@Component
@Description("Deletes identity - ensures core referential integrity.")
public class IdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-delete-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityDeleteProcessor.class);
	//
	@Autowired private IdmIdentityService service;
	@Autowired private IdentityPasswordProcessor passwordProcessor;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private TokenManager tokenManager;
	@Autowired private IdmPasswordHistoryService passwordHistoryService;
	@Autowired private IdmContractSliceService contractSliceService;
	@Autowired private IdmContractSliceGuaranteeService contractSliceGuaranteeService;
	@Autowired private IdmProfileService profileService;
	@Autowired private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired private EntityStateManager entityStateManager;

	public IdentityDeleteProcessor() {
		super(IdentityEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		UUID identityId = identity.getId();
		Assert.notNull(identityId, "Identity ID is required!");
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		//
		// delete contract slices
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setIdentity(identityId);
		contractSliceService.find(sliceFilter, null).forEach(guarantee -> {
			contractSliceService.delete(guarantee);
		});
		// delete contract slice guarantees
		IdmContractSliceGuaranteeFilter sliceGuaranteeFilter = new IdmContractSliceGuaranteeFilter();
		sliceGuaranteeFilter.setGuaranteeId(identityId);
		contractSliceGuaranteeService.find(sliceGuaranteeFilter, null).forEach(guarantee -> {
			contractSliceGuaranteeService.delete(guarantee);
		});
		//
		// contracts
		identityContractService.findAllByIdentity(identityId).forEach(identityContract -> {
			// when identity is deleted, then HR processes has to be skipped (prevent to update deleted identity, when contract is removed)
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			// propagate force attribute
			properties.put(PROPERTY_FORCE_DELETE, forceDelete);
			// prepare event
			IdentityContractEvent contractEvent = new IdentityContractEvent(
					IdentityContractEventType.DELETE, 
					identityContract, 
					properties
			);
			contractEvent.setPriority(PriorityType.HIGH);
			//
			identityContractService.publish(contractEvent);
		});
		
		// delete contract guarantees
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setGuaranteeId(identityId);
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});
		// remove role guarantee
		IdmRoleGuaranteeFilter roleGuaranteeFilter = new IdmRoleGuaranteeFilter();
		roleGuaranteeFilter.setGuarantee(identityId);
		roleGuaranteeService.find(roleGuaranteeFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeService.delete(roleGuarantee);
		});
		// remove password
		passwordProcessor.deletePassword(identity);
		// delete password history for identity
		passwordHistoryService.deleteAllByIdentity(identityId);
		// disable related tokens - tokens has to be disabled to prevent their usage (when tokens are deleted, then token is recreated)
		tokenManager.disableTokens(identity);
		//
		// delete all identity's profiles
		IdmProfileFilter profileFilter = new IdmProfileFilter();
		profileFilter.setIdentityId(identityId);
		profileService.find(profileFilter,  null).forEach(profile -> {
			profileService.delete(profile);
		});
		// remove all IdentityRoleValidRequest for this identity
		List<IdmIdentityRoleValidRequestDto> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentityId(identityId);
		identityRoleValidRequestService.deleteAll(validRequests);
		//
		// delete all identity's delegations - delegate
		IdmDelegationDefinitionFilter delegationFilter = new IdmDelegationDefinitionFilter();
		delegationFilter.setDelegateId(identityId);
		delegationDefinitionService.find(delegationFilter,  null).forEach(delegation -> {
			delegationDefinitionService.delete(delegation);
		});
		//
		// delete all identity's delegations - delegator
		delegationFilter = new IdmDelegationDefinitionFilter();
		delegationFilter.setDelegatorId(identityId);
		delegationDefinitionService.find(delegationFilter,  null).forEach(delegation -> {
			delegationDefinitionService.delete(delegation);
		});
		// deletes identity
		if (forceDelete) {
			LOG.debug("Identity [{}] should be deleted by caller after all asynchronus processes are completed.", identityId);
			//
			// dirty flag only - will be processed after asynchronous events ends
			IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
			stateDeleted.setEvent(event.getId());
			stateDeleted.setResult(
					new OperationResultDto.Builder(OperationState.RUNNING)
						.setModel(new DefaultResultModel(CoreResultCode.DELETED))
						.build()
			);
			entityStateManager.saveState(identity, stateDeleted);
			//
			// set disabled (automatically)
			identity.setState(IdentityState.DISABLED);
			service.saveInternal(identity);
		} else {
			// delete all role requests where is this identity applicant
			IdmRoleRequestFilter roleRequestFilter = new IdmRoleRequestFilter();
			roleRequestFilter.setApplicantId(identityId);
			roleRequestService.find(roleRequestFilter, null).forEach(request ->{
				roleRequestService.delete(request);
			});
			//
			service.deleteInternal(identity);
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
