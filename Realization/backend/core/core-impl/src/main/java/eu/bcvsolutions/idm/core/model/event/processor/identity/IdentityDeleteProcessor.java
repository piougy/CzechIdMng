package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationRecipientRepository;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Delete identity - ensures referential integrity
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity - ensures core referential integrity.")
public class IdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-delete-processor";
	//
	@Autowired private IdmIdentityService service;
	@Autowired private IdentityPasswordProcessor passwordProcessor;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmNotificationRecipientRepository notificationRecipientRepository;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private TokenManager tokenManager;
	@Autowired private IdmPasswordHistoryService passwordHistoryService;
	@Autowired private IdmContractSliceService contractSliceService;
	@Autowired private IdmContractSliceGuaranteeService contractSliceGuaranteeService;
	@Autowired private IdmProfileService profileService;

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
		Assert.notNull(identity.getId(), "Identity ID is required!");
		//
		// delete contract slices
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setIdentity(identity.getId());
		contractSliceService.find(sliceFilter, null).forEach(guarantee -> {
			contractSliceService.delete(guarantee);
		});
		// delete contract slice guarantees
		IdmContractSliceGuaranteeFilter sliceGuaranteeFilter = new IdmContractSliceGuaranteeFilter();
		sliceGuaranteeFilter.setGuaranteeId(identity.getId());
		contractSliceGuaranteeService.find(sliceGuaranteeFilter, null).forEach(guarantee -> {
			contractSliceGuaranteeService.delete(guarantee);
		});
		//
		// contracts
		identityContractService.findAllByIdentity(identity.getId()).forEach(identityContract -> {
			// when identity is deleted, then HR processes has to be shipped (prevent to update deleted identity, when contract is removed)
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			identityContractService.publish(new CoreEvent<>(CoreEventType.DELETE, identityContract, properties));
		});
		// delete contract guarantees
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setGuaranteeId(identity.getId());
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});
		// remove role guarantee
		IdmRoleGuaranteeFilter roleGuaranteeFilter = new IdmRoleGuaranteeFilter();
		roleGuaranteeFilter.setGuarantee(identity.getId());
		roleGuaranteeService.find(roleGuaranteeFilter, null).forEach(roleGuarantee -> {
			roleGuaranteeService.delete(roleGuarantee);
		});
		// remove password
		passwordProcessor.deletePassword(identity);
		// delete password history for identity
		passwordHistoryService.deleteAllByIdentity(identity.getId());
		// set to null all notification recipients - real recipient remains (email etc.)
		notificationRecipientRepository.clearIdentity(identity.getId());
		// remove related tokens
		tokenManager.deleteTokens(identity);
		//
		// Delete all role requests where is this identity applicant
		IdmRoleRequestFilter roleRequestFilter = new IdmRoleRequestFilter();
		roleRequestFilter.setApplicantId(identity.getId());
		roleRequestService.find(roleRequestFilter, null).forEach(request ->{
			roleRequestService.delete(request);
		});
		//
		// delete all identity's profiles
		IdmProfileFilter profileFilter = new IdmProfileFilter();
		profileFilter.setIdentityId(identity.getId());
		profileService.find(profileFilter,  null).forEach(profile -> {
			profileService.delete(profile);
		});
		// remove all IdentityRoleValidRequest for this identity
		List<IdmIdentityRoleValidRequestDto> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentityId(identity.getId());
		identityRoleValidRequestService.deleteAll(validRequests);
		// deletes identity
		service.deleteInternal(identity);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
