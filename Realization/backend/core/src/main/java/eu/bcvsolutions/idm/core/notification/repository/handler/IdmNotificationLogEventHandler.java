package eu.bcvsolutions.idm.core.notification.repository.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.service.NotificationLogService;

/**
 * Sending notifications through rest
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
	
	@HandleAfterCreate
	public void handleAfterCreate(IdmNotificationLog notificationLog) {
		log.debug("Notification log [{}] was created and notificatio will be send.", notificationLog);
		notificationLogService.sendNotificationLog(notificationLog);
	}	
}
