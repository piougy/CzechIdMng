package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioing after identity contract is saved or deleted
 * 
 * TODO: execute provisioning for subordinates - depends on configuration property 
 * 
 * @author Radek Tomiška
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioing after identity contract is saved or deleted.")
public class IdentityContractProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityContractDto> {

	public static final String PROCESSOR_NAME = "identity-contract-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractProvisioningProcessor.class);
	//
	@Autowired private ProvisioningService provisioningService;
	@Autowired private LookupService lookupService;
	
	public IdentityContractProvisioningProcessor() {
		super(CoreEventType.CREATE, CoreEventType.UPDATE, CoreEventType.DELETE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmIdentityContractDto contract) {
		IdmIdentity identity = (IdmIdentity) lookupService.lookupEntity(IdmIdentity.class, contract.getIdentity());
		LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
		provisioningService.doProvisioning(identity);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}	
}