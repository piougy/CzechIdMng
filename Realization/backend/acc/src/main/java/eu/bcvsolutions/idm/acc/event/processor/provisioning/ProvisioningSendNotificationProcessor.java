package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.UniformPasswordManager;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Processor sends notification after success provisioning create event.
 * Only for system entity = IDENTITY. System must has mapped attribute __PASSWORD__
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Vít Švanda
 */
@Component(ProvisioningSendNotificationProcessor.PROCESSOR_NAME)
@Description("After success provisioning send notification to identity with new generate password.")
public class ProvisioningSendNotificationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningSendNotificationProcessor.class);
	public static final String PROCESSOR_NAME = "provisioning-send-notification-processor";
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final IdmIdentityService identityService;
	private final SysSystemService systemService;
	@Autowired
	private UniformPasswordManager uniformPasswordManager;
	
	@Autowired
	public ProvisioningSendNotificationProcessor(NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService,
			IdmIdentityService identityService,
			SysSystemService systemService) {
		super(ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE); // Listen both types => see #conditional.
		//
		Assert.notNull(notificationManager, "Manager is required.");
		Assert.notNull(provisioningOperationService, "Service is required.");
		Assert.notNull(identityService, "Service is required.");
		Assert.notNull(systemService, "Service is required.");
		//
		this.identityService = identityService;
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
		this.systemService = systemService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<SysProvisioningOperationDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		
		// Notification can be send, only when account is created => update can be switched to create, if target account does not exist.
		// @see PrepareConnectorObjectProcessor
		if (provisioningOperation.getOperationType() != ProvisioningEventType.CREATE) {
			return false;
		}
		
		// Notification can be send only if provisioning operation ended successfully!
		if (OperationState.EXECUTED != provisioningOperation.getResultState()) {
			LOG.warn(
					"Notification with password wasn't send, because provisioning result wasn't in the EXECUTED state [{}]!",
					provisioningOperation.getResultState());

			return false;
		}

		if (provisioningOperation.getEntityIdentifier() != null && SystemEntityType.IDENTITY == provisioningOperation.getEntityType()) {
			// Uniform password notification will be send after end of sync.
			IdmEntityStateDto uniformPasswordState = uniformPasswordManager
					.getEntityState(provisioningOperation.getEntityIdentifier(), IdmIdentityDto.class, provisioningOperation.getTransactionId());
			if (uniformPasswordState != null) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		String uid = provisioningOperationService.getByProvisioningOperation(provisioningOperation).getUid();
		IdmIdentityDto identity = null;
		if (provisioningOperation.getEntityIdentifier() != null && SystemEntityType.IDENTITY == provisioningOperation.getEntityType()) {
			identity = identityService.get(provisioningOperation.getEntityIdentifier());
		}
		//
		if (identity != null && identity.getState() != IdentityState.CREATED) {
			for (IcAttribute attribute : provisioningOperationService.getFullConnectorObject(provisioningOperation).getAttributes()) {
				if (attribute instanceof IcPasswordAttribute && attribute.getValue() != null) {
					GuardedString password = ((IcPasswordAttribute) attribute).getPasswordValue();
					//
					// send message with new password to identity, topic has connection to templates
					SysSystemDto system = systemService.get(provisioningOperation.getSystem());
					notificationManager.send(
							AccModuleDescriptor.TOPIC_NEW_PASSWORD,
							new IdmMessageDto.Builder()
							.setLevel(NotificationLevel.SUCCESS)
							.addParameter("systemName", system.getName())
							.addParameter("uid", uid)
							.addParameter("password", password)
							.addParameter("identity", identity)
							.build(),
							identity);

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
