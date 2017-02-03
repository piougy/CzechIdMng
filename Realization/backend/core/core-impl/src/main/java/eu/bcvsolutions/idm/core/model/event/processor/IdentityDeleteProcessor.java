package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
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
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	private final IdmNotificationRecipientRepository notificationRecipientRepository;
	
	@Autowired
	public IdentityDeleteProcessor(
			IdmIdentityRepository repository,
			FormService formService,
			IdentityPasswordProcessor passwordProcessor,
			IdmRoleGuaranteeRepository roleGuaranteeRepository,
			IdmIdentityRoleRepository identityRoleRepository,
			IdmIdentityContractRepository identityContractRepository,
			IdmNotificationRecipientRepository notificationRecipientRepository) {
		super(IdentityEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(formService);
		Assert.notNull(passwordProcessor);
		Assert.notNull(roleGuaranteeRepository);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(identityContractRepository);
		Assert.notNull(notificationRecipientRepository);
		//
		this.repository = repository;
		this.formService = formService;
		this.passwordProcessor = passwordProcessor;
		this.roleGuaranteeRepository = roleGuaranteeRepository;
		this.identityRoleRepository = identityRoleRepository;
		this.identityContractRepository = identityContractRepository;
		this.notificationRecipientRepository = notificationRecipientRepository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		//
		// clear referenced roles
		identityRoleRepository.deleteByIdentity(identity);
		// contracts
		identityContractRepository.deleteByIdentity(identity);
		// contract guaratee - set to null
		identityContractRepository.clearGuarantee(identity);
		// remove role guarantee
		roleGuaranteeRepository.deleteByGuarantee(identity);
		// remove password from confidential storage
		passwordProcessor.deletePassword(identity);
		// delete eav attrs
		formService.deleteValues(identity);
		// set to null all notification recipients - real recipient remains (email etc.)
		notificationRecipientRepository.clearIdentity(identity);
		// deletes identity
		repository.delete(identity);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}