package eu.bcvsolutions.idm.acc.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Do provisioning for {@link IdmIdentityDto} that own contract for that will be
 * added new {@link IdmContractGuaranteeDto}. Provisioning is made before
 * contract guarantee will be added
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Do provisioning for identity before contract guarantee delete.")
public class ContractGuaranteeDeleteProvisioningProcessor extends CoreEventProcessor<IdmContractGuaranteeDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ContractGuaranteeDeleteProvisioningProcessor.class);

	public static final String PROCESSOR_NAME = "contract-guarantee-delete";

	private final ProvisioningService provisioningService;
	private final IdmIdentityService identityService;
	private final IdmIdentityContractService identityContractService;

	@Autowired
	public ContractGuaranteeDeleteProvisioningProcessor(ProvisioningService provisioningService,
			IdmIdentityService identityService, IdmIdentityContractService identityContractService) {
		super(ContractGuaranteeEventType.DELETE);
		//
		Assert.notNull(provisioningService);
		Assert.notNull(identityService);
		Assert.notNull(identityContractService);
		//
		this.provisioningService = provisioningService;
		this.identityService = identityService;
		this.identityContractService = identityContractService;
	}

	@Override
	public EventResult<IdmContractGuaranteeDto> process(EntityEvent<IdmContractGuaranteeDto> event) {
		Object skipProvisioning = event.getProperties().get(ProvisioningService.SKIP_PROVISIONING);
		//
		// skip?
		if (skipProvisioning instanceof Boolean && (Boolean) skipProvisioning) {
			return new DefaultEventResult<>(event, this);
		}
		//
		IdmContractGuaranteeDto contractGuarantee = event.getContent();
		IdmIdentityContractDto contract = identityContractService.get(contractGuarantee.getIdentityContract());
		IdmIdentityDto identity = identityService.get(contract.getIdentity());
		//
		LOG.debug("Do provisioning for identity [{}], contract guarantee will be removed.", identity.getUsername());
		//
		provisioningService.doProvisioning(identity);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 100;
	}
}
