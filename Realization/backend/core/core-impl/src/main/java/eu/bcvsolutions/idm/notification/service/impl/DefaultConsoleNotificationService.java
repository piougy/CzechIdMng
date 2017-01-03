package eu.bcvsolutions.idm.notification.service.impl;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.repository.IdmConsoleLogRepository;
import eu.bcvsolutions.idm.notification.service.api.ConsoleNotificationService;

/**
 * For testing purpose only - prints message to console and persists log
 * 
 * @author Radek Tomiška 
 *
 */
@Component("consoleNotificationService")
public class DefaultConsoleNotificationService extends AbstractNotificationService<IdmConsoleLog> implements ConsoleNotificationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConsoleNotificationService.class);
	
	@Autowired
	public DefaultConsoleNotificationService(IdmConsoleLogRepository repository) {
		super(repository);
	}
	
	@Override
	@Transactional
	public boolean send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		LOG.info("Sending notification to console [{}]", notification);
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
		notificationLog.setSent(new DateTime());
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
		return save(notificationLog);
	}
}
