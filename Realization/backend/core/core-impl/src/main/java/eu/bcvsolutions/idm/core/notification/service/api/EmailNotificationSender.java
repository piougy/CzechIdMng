package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška 
 *
 */
public interface EmailNotificationSender extends NotificationSender<IdmEmailLogDto> {
	
}
