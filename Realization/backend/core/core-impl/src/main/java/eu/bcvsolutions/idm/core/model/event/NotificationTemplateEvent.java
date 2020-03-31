package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;


import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * Events for notification templates
 * 
 * @author Ondrej Husnik
 *
 */
public class NotificationTemplateEvent extends CoreEvent<IdmNotificationTemplateDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core notification template events
	 *
	 */
	public enum NotificationTemplateEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public NotificationTemplateEvent(NotificationTemplateEventType operation, IdmNotificationTemplateDto content) {
		super(operation, content);
	}

	public NotificationTemplateEvent(NotificationTemplateEventType operation, IdmNotificationTemplateDto content,
			Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}