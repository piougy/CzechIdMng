package eu.bcvsolutions.idm.core.notification.service.impl;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.service.ConsoleNotificationSender;
import eu.bcvsolutions.idm.core.notification.api.service.IdmConsoleLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;

/**
 * Prints notification to console and persists log
 * 
 * @author Radek Tomiška 
 *
 */
@Component("consoleNotificationSender")
public class DefaultConsoleNotificationSender extends AbstractNotificationSender<IdmConsoleLogDto> implements ConsoleNotificationSender {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConsoleNotificationSender.class);
	private final IdmConsoleLogService consoleService;
	
	@Autowired
	public DefaultConsoleNotificationSender(IdmConsoleLogService consoleService) {
		Assert.notNull(consoleService);
		//
		this.consoleService = consoleService;
	}
	
	@Override
	public String getType() {
		return IdmConsoleLog.NOTIFICATION_TYPE;
	}
	
	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return consoleService.getEntityClass();
	}

	@Override
	@Transactional
	public IdmConsoleLogDto send(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		LOG.info("Sending notification to console [{}]", notification);
		IdmConsoleLogDto log = createLog(notification);
		final String message = MessageFormat.format("Sending notification [{0}]",
			createLogForSend(notification, true));
		LOG.info(message);
		notification.setType(IdmConsoleLog.NOTIFICATION_TYPE);
		return log;
	}

	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmConsoleLogDto createLog(IdmNotificationDto notification) {
		return consoleService.save(createLogForSend(notification, false));
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
