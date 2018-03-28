package eu.bcvsolutions.idm.acc.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Publish change (~do provisioning) for {@link IdmIdentityDto} that own contract for that will be
 * added new {@link IdmContractGuaranteeDto}. Provisioning is made after
 * contract guarantee is deleted.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Publish change (~do provisioning) for identity after contract guarantee delete.")
public class ContractGuaranteeDeleteProvisioningProcessor extends CoreEventProcessor<IdmContractGuaranteeDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ContractGuaranteeDeleteProvisioningProcessor.class);

	public static final String PROCESSOR_NAME = "contract-guarantee-delete";
	//
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private EntityEventManager entityEventManager;

	public ContractGuaranteeDeleteProvisioningProcessor() {
		super(ContractGuaranteeEventType.DELETE);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmContractGuaranteeDto> event) {
		// Skip provisioning
		// TODO: this is potentially dangerous - how is provisioning executed otherwise?
		return !this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());
	}

	@Override
	public EventResult<IdmContractGuaranteeDto> process(EntityEvent<IdmContractGuaranteeDto> event) {
		IdmContractGuaranteeDto contractGuarantee = event.getContent();
		IdmIdentityContractDto contract = identityContractService.get(contractGuarantee.getIdentityContract());
		//
		LOG.debug("Publish change for identity [{}], contract guarantee will be added.", contract.getIdentity());
		//
		entityEventManager.changedEntity(IdmIdentityDto.class, contract.getIdentity(), event);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
