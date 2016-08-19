package eu.bcvsolutions.idm.notification.service.impl;

import java.text.MessageFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.repository.IdmConsoleLogRepository;
import eu.bcvsolutions.idm.notification.service.ConsoleNotificationService;

/**
 * For testing purpose only
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Component("consoleNotificationService")
public class DefaultConsoleNotificationService extends AbstractNotificationService implements ConsoleNotificationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConsoleNotificationService.class);
	
	@Autowired
	private IdmConsoleLogRepository logRepository;
	
	@Override
	public boolean send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		log.info("Sending notification to console [{}]", notification);
		IdmNotification log = createLog(notification);
		System.out.println(MessageFormat.format("Sending notification [{0}]", log));
		return true;
	}
	
	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotification createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		IdmConsoleLog notificationLog = new IdmConsoleLog();
		notificationLog.setSent(new Date());
		notificationLog.setParent(notification);
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients - real recipient is console
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients().add(new IdmNotificationRecipient(notificationLog, recipient.getIdentityRecipient(), NOTIFICATION_TYPE));
		});
		// clone from - resolve real email
		if (notification.getSender() != null) {
			notificationLog.setSender(notification.getSender());
		}
		return logRepository.save(notificationLog);
	}
}
