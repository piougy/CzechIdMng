package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Extend default notification service for simle noticitaion log sending
 * 
 * @author Radek Tomiška 
 *
 */
public interface NotificationLogService extends NotificationService {
	
	public static final String NOTIFICATION_TYPE = "notification";
	
	/**
	 * Sends existing notification to routing
	 * 
	 * @param notificationLog
	 * @return
	 */
	boolean sendNotificationLog(IdmNotificationLog notificationLog);

}
