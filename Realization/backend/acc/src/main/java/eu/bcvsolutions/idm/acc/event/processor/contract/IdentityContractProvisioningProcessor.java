package eu.bcvsolutions.idm.acc.event.processor.contract;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioning after identity contract is saved or deleted. 
 * Executes provisioning for all contract's subordinates (newly added and previous) - depends on configuration property.
 * 
 * @author Radek Tomiška
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after identity contract is saved or deleted.")
public class IdentityContractProvisioningProcessor extends AbstractIdentityContractProvisioningProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractProvisioningProcessor.class);
	//
	@Autowired private ProvisioningService provisioningService;
	@Autowired private LookupService lookupService;	
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentity identity = (IdmIdentity) lookupService.lookupEntity(IdmIdentity.class, event.getContent().getIdentity());
		LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
		provisioningService.doProvisioning(identity);
		//
		// execute provisioning for all subordinates by given contract
		if (isIncludeSubordinates()) {
			Set<UUID> originalSubordinates = (Set<UUID>) event.getProperties().get(PROPERTY_PREVIOUS_SUBORDINATES);
			findAllSubordinates(identity.getId())
				.forEach(subordinate -> {
					if (originalSubordinates != null && originalSubordinates.contains(subordinate.getId())) {
						originalSubordinates.remove(subordinate.getId());
					} else {
						LOG.debug("Call provisioning for identity's [{}] newly assigned subordinate [{}]", identity.getUsername(), subordinate.getUsername());
						provisioningService.doProvisioning(subordinate);
					}
				});
			if (originalSubordinates != null) {
				originalSubordinates.forEach(originalSubordinateId -> {
					IdmIdentity originalSubordinate = (IdmIdentity) lookupService.lookupEntity(IdmIdentity.class, originalSubordinateId);
					LOG.debug("Call provisioning for identity's [{}] previous subordinate [{}]", identity.getUsername(), originalSubordinate.getUsername());
					provisioningService.doProvisioning(originalSubordinate);
				});
			}
		}
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}	
}