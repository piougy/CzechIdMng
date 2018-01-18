package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity's password change notification
 * 
 * @author Patrik Stloukal
 *
 */
@Component
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Description("Sends notification to identity of changed password.")
public class IdentityPasswordChangeNotificationProcessor extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	private static final String CZECHIDM_NAME = "CzechIdM";
	public static final String PROCESSOR_NAME = "identity-password-change-notification";

	private final NotificationManager notificationManager;

	@Autowired
	public IdentityPasswordChangeNotificationProcessor(NotificationManager notificationManager) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(notificationManager);
		//
		this.notificationManager = notificationManager;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		List<EventResult<IdmIdentityDto>> results = event.getContext().getResults();
		List<String> systems = new ArrayList<>();
		for (EventResult<IdmIdentityDto> result : results) {
			result.getResults().stream().filter(res -> {
				return res.getModel().getStatusEnum().equals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS.name());
			}).forEach(res -> {
				IdmAccountDto account = (IdmAccountDto) res.getModel().getParameters().get(IdmAccountDto.PARAMETER_NAME);
				if (!account.isIdm()) {
					systems.add(account.getSystemName());
				} else {
					systems.add(CZECHIDM_NAME);
				}
			});
		}
		if (!systems.isEmpty()) {
			sendNotification(identity, systems);
		}
		return new DefaultEventResult<>(event, this);

	}

	@Override
	public int getOrder() {
		return super.getOrder() + 1500;
	}

	private void sendNotification(IdmIdentityDto identity, List<String> accounts) {
		notificationManager.send(CoreModuleDescriptor.TOPIC_PASSWORD_CHANGED,
				new IdmMessageDto.
					Builder()
						.setLevel(NotificationLevel.INFO)
						.addParameter("username", identity.getUsername())
						.addParameter("accounts", accounts)
					.build(),
				identity);
	}
}
