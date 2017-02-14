package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;

@Component
@Description("After success provisioning send notification to identity with new generate password.")
public class ProvisioningSendNotificationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	public static final String PROCESSOR_NAME = "provisioning-send-notification-processor";
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final IdmIdentityService identityService;
	
	@Autowired
	public ProvisioningSendNotificationProcessor(NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService,
			IdmIdentityService identityService) {
		super(ProvisioningEventType.CREATE);
		//
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(identityService);
		//
		this.identityService = identityService;
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		IdmIdentity identity = identityService.get(provisioningOperation.getEntityIdentifier());
		for (IcAttribute attribute : provisioningOperationService.getFullConnectorObject(provisioningOperation).getAttributes()) {
			// TODO: send password always, when create?
			if (attribute instanceof IcPasswordAttribute && attribute.getValue() != null) {
				GuardedString password = ((IcPasswordAttribute) attribute).getPasswordValue();
				// send message with new password to identity
				notificationManager.send(AccModuleDescriptor.TOPIC_NEW_PASSWORD, new IdmMessage.Builder()
						.setLevel(NotificationLevel.SUCCESS)
						.setSubject("Provisioning success")
						.setHtmlMessage(
								"Provisioning on system: " + provisioningOperation.getSystem().getName() + ", with uid " + provisioningOperation.getSystemEntityUid() + ", was success. There is your new awesome password: '" + password.asString() + "'").build(), identity);
				break;
			}
			
		}
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after create
		return 1000;
	}

}
