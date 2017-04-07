package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Save identity, catch event UPDATE and CREATE
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity.")
public class IdentitySaveProcessor extends CoreEventProcessor<IdmIdentity> {

	public static final String PROCESSOR_NAME = "identity-save-processor";
	private final IdmIdentityRepository repository;
	private final IdentityPasswordProcessor passwordProcessor;
	private final IdmIdentityContractService identityContractService;
	
	@Autowired
	public IdentitySaveProcessor(
			IdmIdentityRepository repository,
			IdentityPasswordProcessor passwordProcessor,
			IdmIdentityContractService identityContractService) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE);
		//
		Assert.notNull(repository);
		Assert.notNull(passwordProcessor);
		Assert.notNull(identityContractService);
		//
		this.repository = repository;
		this.passwordProcessor = passwordProcessor;
		this.identityContractService = identityContractService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		GuardedString password = identity.getPassword();
		
		identity = repository.save(identity);
		//
		// save password
		if (password != null) {
			PasswordChangeDto passwordDto = new PasswordChangeDto();
			passwordDto.setNewPassword(password);
			passwordProcessor.savePassword(identity, passwordDto);
		}
		//
		// create default identity contract
		if (IdentityEventType.CREATE == event.getType()) {
			identityContractService.save(identityContractService.prepareDefaultContract(identity));
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
}