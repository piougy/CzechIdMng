package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška 
 *
 */
public interface EmailNotificationSender extends NotificationSender<IdmEmailLog> {
	
}
