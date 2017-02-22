package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;

@Component
@Description("After success provisioning send notification to identity with new generate password.")
public class ProvisioningSendNotificationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningSendNotificationProcessor.class);
	public static final String PROCESSOR_NAME = "provisioning-send-notification-processor";
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final IdmIdentityService identityService;
	private final IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired
	public ProvisioningSendNotificationProcessor(NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService,
			IdmIdentityService identityService,
			IdmNotificationTemplateService notificationTemplateService) {
		super(ProvisioningEventType.CREATE);
		//
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(identityService);
		Assert.notNull(notificationTemplateService);
		//
		this.identityService = identityService;
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
		this.notificationTemplateService = notificationTemplateService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		IdmIdentity identity = null;
		if (provisioningOperation.getEntityIdentifier() != null && SystemEntityType.IDENTITY == provisioningOperation.getEntityType()) {
			identity = identityService.get(provisioningOperation.getEntityIdentifier());
		}
		// TODO: identity or email null, send message to actual log user?
		if (identity != null) {
			for (IcAttribute attribute : provisioningOperationService.getFullConnectorObject(provisioningOperation).getAttributes()) {
				// TODO: send password always, when create?
				if (attribute instanceof IcPasswordAttribute && attribute.getValue() != null) {
					GuardedString password = ((IcPasswordAttribute) attribute).getPasswordValue();
					// prepare parameters for message template
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("systemName", provisioningOperation.getSystem().getName());
					parameters.put("uid", provisioningOperation.getSystemEntityUid());
					parameters.put("password", password);
					//
					IdmNotificationTemplate template = notificationTemplateService.getTemplateByCode("prov_pass");
					//
					if (template != null) {
						// send message with new password to identity
						notificationManager.send(
								AccModuleDescriptor.TOPIC_NEW_PASSWORD,
								template,
								parameters, 
								identity);
					} else {
						// log missing templates
						LOG.info("[SysProvisioningOperation] Password for new created account was not sent, missing notification template! Operation id: [{}].", provisioningOperation.getId());
					}
					break;
				}
				
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
