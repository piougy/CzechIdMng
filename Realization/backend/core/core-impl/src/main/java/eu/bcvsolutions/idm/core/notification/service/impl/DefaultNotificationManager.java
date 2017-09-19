package eu.bcvsolutions.idm.core.notification.service.impl;

import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;

/**
 * Sends notifications
 * 
 * @author Radek Tomiška
 *
 */
@Component("notificationManager")
public class DefaultNotificationManager extends AbstractNotificationSender<IdmNotificationLogDto>
		implements NotificationManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultNotificationManager.class);
	private final IdmNotificationLogService notificationLogService;
	private final ProducerTemplate producerTemplate;

	@Autowired
	public DefaultNotificationManager(IdmNotificationLogService notificationLogService,
			ProducerTemplate producerTemplate) {
		Assert.notNull(notificationLogService);
		Assert.notNull(producerTemplate);
		//
		this.notificationLogService = notificationLogService;
		this.producerTemplate = producerTemplate;
	}

	@Override
	public String getType() {
		return IdmNotificationLog.NOTIFICATION_TYPE;
	}

	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return notificationLogService.getEntityClass();
	}

	@Override
	@Transactional
	public IdmNotificationLogDto send(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		IdmNotificationLogDto notificationLog = createLog(notification);
		return sendNotificationLog(notificationLog);
	}

	/**
	 * Sends existing notification to routing
	 * 
	 * @param notificationLog
	 * @return
	 */
	private IdmNotificationLogDto sendNotificationLog(IdmNotificationLogDto notificationLog) {
		LOG.info("Sending notification [{}]", notificationLog);
		// send notification to routing
		producerTemplate.sendBody("direct:notifications", notificationLog);
		return notificationLog;
	}

	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotificationLogDto createLog(IdmNotificationDto notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		// we can only create log, if notification is instance of
		// IdmNotificationLog
		if (notification instanceof IdmNotificationLogDto) {
			notification.setSent(new DateTime());
			return notificationLogService.save((IdmNotificationLogDto) notification);
		}
		// we need to clone notification
		IdmNotificationLogDto notificationLog = new IdmNotificationLogDto();
		notificationLog.setSent(new DateTime());
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients()
					.add(cloneRecipient(notificationLog, recipient, recipient.getRealRecipient()));
		});
		notificationLog.setIdentitySender(notification.getIdentitySender());
		return notificationLogService.save(notificationLog);
	}

}
