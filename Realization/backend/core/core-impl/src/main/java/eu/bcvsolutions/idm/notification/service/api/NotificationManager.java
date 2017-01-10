package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Sends notification over all registered sender by notification configuration.
 * 
 * @author Radek Tomiška
 *
 */
public interface NotificationManager extends NotificationSender<IdmNotificationLog> {

}
