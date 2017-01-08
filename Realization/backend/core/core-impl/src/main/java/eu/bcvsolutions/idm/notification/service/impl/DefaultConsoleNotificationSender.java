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
import eu.bcvsolutions.idm.notification.service.api.ConsoleNotificationSender;
import eu.bcvsolutions.idm.notification.service.api.IdmConsoleLogService;

/**
 * Prints notification to console and persists log
 * 
 * @author Radek Tomiška 
 *
 */
@Component("consoleNotificationService")
public class DefaultConsoleNotificationSender extends AbstractNotificationSender<IdmConsoleLog> implements ConsoleNotificationSender {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConsoleNotificationSender.class);
	private final IdmConsoleLogService emailConsoleService;
	
	@Autowired
	public DefaultConsoleNotificationSender(IdmConsoleLogService emailConsoleService) {
		Assert.notNull(emailConsoleService);
		//
		this.emailConsoleService = emailConsoleService;
	}
	
	@Override
	@Transactional
	public IdmConsoleLog send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		LOG.info("Sending notification to console [{}]", notification);
		IdmConsoleLog log = createLog(notification);
		System.out.println(MessageFormat.format("Sending notification [{0}]", log));
		return log;
	}
	
	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmConsoleLog createLog(IdmNotification notification) {
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
			notificationLog.getRecipients().add(new IdmNotificationRecipient(notificationLog, recipient.getIdentityRecipient(), IdmConsoleLog.NOTIFICATION_TYPE));
		});
		// clone from - resolve real email
		notificationLog.setIdentitySender(notification.getIdentitySender());
		//
		return emailConsoleService.save(notificationLog);
	}
}
