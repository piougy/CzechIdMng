package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Provisioning start event processor
 * 
 * @author svandav
 *
 */
@Component
@Description("Starts provisioning process by given account and entity")
public class ProvisioningStartProcessor extends AbstractEntityEventProcessor<AccAccount> {

	public static final String PROCESSOR_NAME = "provisioning-start-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningStartProcessor.class);
	private final ProvisioningService provisioningService;

	@Autowired
	public ProvisioningStartProcessor(ProvisioningService provisioningService) {
		super(ProvisioningEventType.START);
		//
		Assert.notNull(provisioningService);
		//
		this.provisioningService = provisioningService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccount> process(EntityEvent<AccAccount> event) {
		LOG.info("Provisioning event start");
		AccAccount account = event.getContent();
		Assert.notNull(account);

		if (account.isInProtection()) {
			if(!isCanceledProvisioningProtectionBreak(event.getProperties())){
				LOG.info("Account [{}] is in protection. Provisioning is skipped.", account.getUid());
				return new DefaultEventResult<>(event, this);				
			}
			LOG.info("Account [{}] is in protection, but cancle attribute is TRUE. Provisioning is not skipped.", account.getUid());
			provisioningService.doInternalProvisioning(account,
					(AbstractEntity) event.getProperties().get(ProvisioningService.ENTITY_PROPERTY_NAME));
		}

		provisioningService.doInternalProvisioning(account,
				(AbstractEntity) event.getProperties().get(ProvisioningService.ENTITY_PROPERTY_NAME));
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	private boolean isCanceledProvisioningProtectionBreak(Map<String, Serializable> properties) {
		Object breakProvisioning = properties.get(ProvisioningService.CANCEL_PROVISIONING_BREAK_IN_PROTECTION);
		if (breakProvisioning != null && breakProvisioning instanceof Boolean && (Boolean) breakProvisioning) {
			return true;
		}
		return false;
	}
}