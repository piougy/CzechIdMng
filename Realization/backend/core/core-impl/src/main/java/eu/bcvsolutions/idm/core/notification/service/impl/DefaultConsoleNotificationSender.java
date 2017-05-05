package eu.bcvsolutions.idm.core.notification.service.impl;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.notification.service.api.ConsoleNotificationSender;
import eu.bcvsolutions.idm.core.notification.service.api.IdmConsoleLogService;

/**
 * Prints notification to console and persists log
 * 
 * @author Radek Tomiška 
 *
 */
@Component("consoleNotificationSender")
public class DefaultConsoleNotificationSender extends AbstractNotificationSender<IdmConsoleLogDto> implements ConsoleNotificationSender {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConsoleNotificationSender.class);
	private final IdmConsoleLogService emailConsoleService;
	
	@Autowired
	public DefaultConsoleNotificationSender(IdmConsoleLogService emailConsoleService) {
		Assert.notNull(emailConsoleService);
		//
		this.emailConsoleService = emailConsoleService;
	}
	
	@Override
	public String getType() {
		return IdmConsoleLog.NOTIFICATION_TYPE;
	}

	@Override
	@Transactional
	public IdmConsoleLogDto send(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		LOG.info("Sending notification to console [{}]", notification);
		IdmConsoleLogDto log = createLog(notification);
		System.out.println(MessageFormat.format("Sending notification [{0}]", createLogForSend(notification, true)));
		return log;
	}
	
	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmConsoleLogDto createLog(IdmNotificationDto notification) {
		return emailConsoleService.save(createLogForSend(notification, false));
	}
	
	/**
	 * Create email log for send, this record is not persist.
	 * 
	 * @param notification
	 * @param showGuardedString
	 * @return
	 */
	private IdmConsoleLogDto createLogForSend(IdmNotificationDto notification, boolean showGuardedString) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		IdmConsoleLogDto notificationLog = new IdmConsoleLogDto();
		notificationLog.setSent(new DateTime());
		notificationLog.setParent(notification.getId());
		// clone message
		notificationLog.setMessage(getMessage(notification, showGuardedString));
		// clone recipients - real recipient is console
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients().add(new IdmNotificationRecipientDto(notificationLog.getId(), recipient.getIdentityRecipient(), IdmConsoleLog.NOTIFICATION_TYPE));
		});
		// clone from - resolve real email
		notificationLog.setIdentitySender(notification.getIdentitySender());
		return notificationLog;
	}
}
