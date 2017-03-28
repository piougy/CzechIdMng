package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationRecipientRepository;

/**
 * Delete identity - ensures referential integrity
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity.")
public class IdentityDeleteProcessor extends CoreEventProcessor<IdmIdentity> {

	public static final String PROCESSOR_NAME = "identity-delete-processor";
	private final IdmIdentityRepository repository;
	private final FormService formService;
	private final IdentityPasswordProcessor passwordProcessor;
	private final IdmRoleGuaranteeRepository roleGuaranteeRepository;
	private final IdmIdentityContractService identityContractService;
	private final IdmNotificationRecipientRepository notificationRecipientRepository;
	private final IdmRoleRequestService roleRequestService;
	private final IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	
	@Autowired
	public IdentityDeleteProcessor(
			IdmIdentityRepository repository,
			FormService formService,
			IdentityPasswordProcessor passwordProcessor,
			IdmRoleGuaranteeRepository roleGuaranteeRepository,
			IdmIdentityContractService identityContractService,
			IdmNotificationRecipientRepository notificationRecipientRepository,
			IdmRoleRequestService roleRequestService,
			IdmIdentityRoleValidRequestService identityRoleValidRequestService) {
		super(IdentityEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(formService);
		Assert.notNull(passwordProcessor);
		Assert.notNull(roleGuaranteeRepository);
		Assert.notNull(identityContractService);
		Assert.notNull(notificationRecipientRepository);
		Assert.notNull(roleRequestService);
		Assert.notNull(identityRoleValidRequestService);
		//
		this.repository = repository;
		this.formService = formService;
		this.passwordProcessor = passwordProcessor;
		this.roleGuaranteeRepository = roleGuaranteeRepository;
		this.identityContractService = identityContractService;
		this.notificationRecipientRepository = notificationRecipientRepository;
		this.roleRequestService = roleRequestService;
		this.identityRoleValidRequestService = identityRoleValidRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		// contracts
		identityContractService.getContracts(identity).forEach(identityContract -> {
			identityContractService.delete(identityContract);
		});
		// contract guaratee - set to null
		identityContractService.clearGuarantee(identity);
		// remove role guarantee
		roleGuaranteeRepository.deleteByGuarantee(identity);
		// remove password from confidential storage
		passwordProcessor.deletePassword(identity);
		// delete eav attrs
		formService.deleteValues(identity);
		// set to null all notification recipients - real recipient remains (email etc.)
		notificationRecipientRepository.clearIdentity(identity);
		
		// Delete all role requests where is this identity applicant
		RoleRequestFilter roleRequestFilter = new RoleRequestFilter();
		roleRequestFilter.setApplicantId(identity.getId());
		roleRequestService.findDto(roleRequestFilter, null).forEach(request ->{
			roleRequestService.delete(request);
		});
		
		// remove all IdentityRoleValidRequest for this identity
		List<IdmIdentityRoleValidRequest> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentity(identity);
		identityRoleValidRequestService.deleteAll(validRequests);
		
		// deletes identity
		repository.delete(identity);
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}