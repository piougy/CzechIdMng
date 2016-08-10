package eu.bcvsolutions.idm.notification.service;

import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Extend default notification service for simle noticitaion log sending
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface NotificationLogService extends NotificationService {
	
	public static final String NOTIFICATION_TYPE = "notification";
	
	/**
	 * Sends existing notification to routing
	 * 
	 * @param notification
	 * @return
	 */
	boolean sendNotificationLog(IdmNotificationLog notificationLog);

}
