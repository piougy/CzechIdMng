package eu.bcvsolutions.idm.acc.event.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity provisioning after role has been deleted.
 *
 * @author Jan Helbich
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after identity role is deleted.")
public class IdentityRoleDeleteProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-provisioning-processor";
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleDeleteProvisioningProcessor.class);
	private final ProvisioningService provisioningService;
	private final IdmIdentityContractRepository identityContractRepository;

	@Autowired
	public IdentityRoleDeleteProvisioningProcessor(
			ProvisioningService provisioningService,
			IdmIdentityContractRepository identityContractRepository) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(provisioningService);
		Assert.notNull(identityContractRepository);
		//
		this.provisioningService = provisioningService;
		this.identityContractRepository = identityContractRepository;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		
		IdmIdentityRoleDto identityRole = event.getContent();
		IdmIdentityContract identityContract = identityContractRepository.findOne(identityRole.getIdentityContract());
		
		LOG.debug("Call provisioning for identity [{}]", identityContract.getIdentity().getUsername());
		
		provisioningService.doProvisioning(identityContract.getIdentity());
		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}