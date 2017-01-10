package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška 
 *
 */
public interface EmailNotificationSender extends NotificationSender<IdmEmailLog> {
	
}
