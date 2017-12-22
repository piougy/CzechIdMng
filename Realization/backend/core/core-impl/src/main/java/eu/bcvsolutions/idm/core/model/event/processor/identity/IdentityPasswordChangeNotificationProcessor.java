package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
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

	public static final String PROCESSOR_NAME = "identity-password-change-notification";

	private final NotificationManager notificationManager;
	private final String code = "PASSWORD_CHANGE_ACCOUNT_SUCCESS";

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
			for (OperationResult res : result.getResults()) {
				if (this.code.equals(res.getCode())) {
					IdmAccountDto account = (IdmAccountDto) res.getModel().getParameters().get("account");
					if (!account.isIdm()) {
						systems.add(account.getSystemName());
					} else {
						systems.add("CzechIdm");
					}
				}
			}
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
				new IdmMessageDto.Builder().setLevel(NotificationLevel.INFO)
						.addParameter("username", identity.getUsername()).addParameter("accounts", accounts).build(),

				identity);
	}
}
