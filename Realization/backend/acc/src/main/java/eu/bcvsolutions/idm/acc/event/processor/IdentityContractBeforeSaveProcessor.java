package eu.bcvsolutions.idm.acc.event.processor;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Loads and stores previous identity's subordinates to events property - depends on configuration property.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Loads and stores previous identity's subordinates to events property.")
public class IdentityContractBeforeSaveProcessor extends AbstractEntityEventProcessor<IdmIdentityContractDto> {
	
	public static final String PROCESSOR_NAME = "identity-contract-before-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractBeforeSaveProcessor.class);
	//
	private IdentityContractProvisioningProcessor provisioningProcessor;
	
	public IdentityContractBeforeSaveProcessor() {
		super(CoreEventType.CREATE, CoreEventType.UPDATE, CoreEventType.DELETE, CoreEventType.EAV_SAVE);
		//
		provisioningProcessor = AutowireHelper.autowireBean(new IdentityContractProvisioningProcessor());
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		if (provisioningProcessor.isIncludeSubordinates()) {
			// set original subordinates as Set<UUID>
			HashSet<UUID> originalSubordinates = provisioningProcessor
					.findAllSubordinates(event.getContent().getIdentity())
					.stream()
					.map(IdmIdentity::getId)
					.collect(Collectors.toCollection(HashSet::new));
			event.getProperties().put(IdentityContractProvisioningProcessor.PROPERTY_PREVIOUS_SUBORDINATES, originalSubordinates);
			LOG.debug("Previous subordinates found [{}]", originalSubordinates.size());
		}		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return -ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
