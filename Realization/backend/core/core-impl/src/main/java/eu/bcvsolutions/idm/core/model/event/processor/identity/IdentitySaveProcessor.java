package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Save identity, catch event UPDATE and CREATE
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity.")
public class IdentitySaveProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-save-processor";
	private final IdmIdentityService service;
	private final IdentityPasswordProcessor passwordProcessor;
	private final IdmIdentityContractService identityContractService;
	private final IdentityConfiguration identityConfiguration;
	
	@Autowired
	public IdentitySaveProcessor(
			IdmIdentityService service,
			IdentityPasswordProcessor passwordProcessor,
			IdmIdentityContractService identityContractService,
			IdentityConfiguration identityConfiguration) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE);
		//
		Assert.notNull(service);
		Assert.notNull(passwordProcessor);
		Assert.notNull(identityContractService);
		Assert.notNull(identityConfiguration);
		//
		this.service = service;
		this.passwordProcessor = passwordProcessor;
		this.identityContractService = identityContractService;
		this.identityConfiguration = identityConfiguration;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		GuardedString password = identity.getPassword();
		
		identity = service.saveInternal(identity);
		//
		event.setContent(identity);
		//
		// save password
		if (password != null) {
			PasswordChangeDto passwordDto = new PasswordChangeDto();
			passwordDto.setNewPassword(password);
			passwordProcessor.savePassword(identity, passwordDto);
		}
		//
		// create default identity contract
		boolean skipCreationDefaultContract = getBooleanProperty(
				IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION, event.getProperties());
		
		if (!skipCreationDefaultContract && IdentityEventType.CREATE.name() == event.getType().name()
				&& identityConfiguration.isCreateDefaultContractEnabled()) {
			identityContractService.save(identityContractService.prepareMainContract(identity.getId()));
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
}