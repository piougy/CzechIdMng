package eu.bcvsolutions.idm.notification.repository.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.service.NotificationLogService;

/**
 * Sending notifications through rest
 * 
 * @author Radek Tomiška 
 */
@Component
@RepositoryEventHandler(IdmNotificationLog.class)
public class IdmNotificationLogEventHandler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmNotificationLogEventHandler.class);
	
	@Autowired
	private NotificationLogService notificationLogService;

	@HandleBeforeSave
	public void handleBeforeSave(IdmNotificationLog notificationLog) {		
		throw new UnsupportedOperationException("Update notification is not supported. Use POST method for creating new notification");
	}
	
	/**
	 * Send notification after notificationLog is created ...
	 * TODO: switch to handleBeforeCreate and skip standard persist - this could have problem with transactions (notification will be created even sending fails unexpectedly)
	 * 
	 * @param notificationLog
	 */
	@HandleAfterCreate
	public void handleAfterCreate(IdmNotificationLog notificationLog) {
		log.debug("Notification log [{}] was created and notificatio will be send.", notificationLog);
		notificationLogService.sendNotificationLog(notificationLog);
	}	
}
