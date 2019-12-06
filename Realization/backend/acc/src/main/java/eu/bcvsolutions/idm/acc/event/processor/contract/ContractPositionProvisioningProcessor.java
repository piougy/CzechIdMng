package eu.bcvsolutions.idm.acc.event.processor.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioning (for entire identity) after contract position is saved or deleted.
 * 
 * @author Vít Švanda
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after contract position is saved or deleted.")
public class ContractPositionProvisioningProcessor extends CoreEventProcessor<IdmContractPositionDto> {

	public static final String PROCESSOR_NAME = "contract-position-provisioning-processor";
	
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmIdentityContractService identityContractService;

	public ContractPositionProvisioningProcessor() {
		super(ContractPositionEventType.DELETE, ContractPositionEventType.NOTIFY);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractPositionDto> process(EntityEvent<IdmContractPositionDto> event) {

		IdmContractPositionDto contractPositionDto = event.getContent();
		Assert.notNull(contractPositionDto, "Contract position cannot be null!");
		Assert.notNull(contractPositionDto.getIdentityContract(), "Id of contract cannot be null!");

		UUID identityId = null;
		IdmIdentityContractDto contractDto = DtoUtils.getEmbedded(contractPositionDto,
				IdmContractPosition_.identityContract.getName(), IdmIdentityContractDto.class,
				(IdmIdentityContractDto) null);
		if (contractDto == null) {
			contractDto = identityContractService.get(contractPositionDto.getIdentityContract());
		}
		
		identityId = contractDto.getIdentity();
		Assert.notNull(identityId, "Identity ID cannot be null!");
		doProvisioning(identityId, event);

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmContractPositionDto> event) {
		// Skip provisioning
		return !this.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE, event.getProperties());
	}

	private void doProvisioning(UUID identityId, EntityEvent<IdmContractPositionDto> event) {
		// sync
		provisioningService.doProvisioning((IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identityId));

	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}

}