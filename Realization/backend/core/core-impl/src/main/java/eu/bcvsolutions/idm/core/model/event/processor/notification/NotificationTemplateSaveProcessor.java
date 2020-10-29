package eu.bcvsolutions.idm.core.model.event.processor.notification;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.NotificationTemplateProcessor;
import eu.bcvsolutions.idm.core.model.event.NotificationTemplateEvent.NotificationTemplateEventType;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;

/**
 * Saves, updates notification template.
 * 
 * @author Ondrej Husnik
 *
 */
@Component(NotificationTemplateSaveProcessor.PROCESSOR_NAME)
@Description("Saves and updates notification template.")
public class NotificationTemplateSaveProcessor 
		extends CoreEventProcessor<IdmNotificationTemplateDto>
		implements NotificationTemplateProcessor {

	public static final String PROCESSOR_NAME = "notification-template-save-processor";

	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;

	public NotificationTemplateSaveProcessor() {
		super(NotificationTemplateEventType.CREATE, NotificationTemplateEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmNotificationTemplateDto> process(EntityEvent<IdmNotificationTemplateDto> event) {
		IdmNotificationTemplateDto template = event.getContent();
		template = notificationTemplateService.saveInternal(template);
		event.setContent(template);
		return new DefaultEventResult<>(event, this);
	}
}
