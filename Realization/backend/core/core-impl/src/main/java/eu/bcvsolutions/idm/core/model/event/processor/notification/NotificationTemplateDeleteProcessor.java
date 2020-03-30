package eu.bcvsolutions.idm.core.model.event.processor.notification;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.event.NotificationTemplateEvent.NotificationTemplateEventType;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;

/**
 * Deletes notification template - ensures referential integrity.
 * 
 * @author Ondrej Husnik
 *
 */
@Component(NotificationTemplateDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes notification template.")
public class NotificationTemplateDeleteProcessor extends CoreEventProcessor<IdmNotificationTemplateDto>
		implements EntityEventProcessor<IdmNotificationTemplateDto> {

	public static final String PROCESSOR_NAME = "notification-template-delete-processor";

	@Autowired
	private IdmNotificationConfigurationService notificationConfigService;
	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;
	@Autowired
	private IdmNotificationLogService notificationLogService;

	public NotificationTemplateDeleteProcessor() {
		super(NotificationTemplateEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmNotificationTemplateDto> process(EntityEvent<IdmNotificationTemplateDto> event) {
		IdmNotificationTemplateDto template = event.getContent();
		Assert.notNull(template.getId(), "Template id is required!");

		// Check if used in notification configuration
		IdmNotificationConfigurationFilter configFilter = new IdmNotificationConfigurationFilter();
		configFilter.setTemplate(template.getId());
		int configUsingNum = notificationConfigService.find(configFilter, null).getContent().size();
		if (configUsingNum > 0) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TEMPLATE_DELETE_FAILED_USED_CONFIGURATION,
					ImmutableMap.of("template", template.getCode(), "usage", configUsingNum));
		}

		// Check if used in notification
		IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
		notificationFilter.setTemplateId(template.getId());
		int notificationNum = notificationLogService.find(notificationFilter, null).getContent().size();
		if (notificationNum > 0) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TEMPLATE_DELETE_FAILED_USED_NOTIFICATION,
					ImmutableMap.of("template", template.getCode(), "usage", notificationNum));
		}

		notificationTemplateService.deleteInternal(template);
		return new DefaultEventResult<>(event, this);
	}
}
