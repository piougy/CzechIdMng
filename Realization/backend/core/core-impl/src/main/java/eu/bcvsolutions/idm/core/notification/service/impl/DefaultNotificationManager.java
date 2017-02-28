package eu.bcvsolutions.idm.core.notification.service.impl;

import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;

/**
 * Sends notifications
 * 
 * @author Radek Tomiška
 *
 */
@Component("notificationManager")
public class DefaultNotificationManager extends AbstractNotificationSender<IdmNotificationLog> implements NotificationManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultNotificationManager.class);
	private final IdmNotificationLogService notificationLogService;
    private final ProducerTemplate producerTemplate;
	
    @Autowired
	public DefaultNotificationManager(
			IdmNotificationLogService notificationLogService,
			ProducerTemplate producerTemplate
			) {
    	Assert.notNull(notificationLogService);
    	Assert.notNull(producerTemplate);
    	//
    	this.notificationLogService = notificationLogService;
    	this.producerTemplate = producerTemplate;
	}
	
	@Override
	@Transactional
	public IdmNotificationLog send(IdmNotification notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		IdmNotificationLog notificationLog = (IdmNotificationLog) createLog(notification);
		return sendNotificationLog(notificationLog);
	}
	
	/**
	 * Sends existing notification to routing
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotificationLog sendNotificationLog(IdmNotificationLog notificationLog) {
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
	private IdmNotification createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		// we can only create log, if notification is instance of IdmNotificationLog
		if (notification instanceof IdmNotificationLog) {
			notification.setSent(new DateTime());
			return notificationLogService.save((IdmNotificationLog) notification);
		}
		// we need to clone notification
		IdmNotificationLog notificationLog = new IdmNotificationLog();
		notificationLog.setSent(new DateTime());
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients().add(cloneRecipient(notificationLog, recipient));
		});
		notificationLog.setIdentitySender(notification.getIdentitySender());
		return notificationLogService.save(notificationLog);
	}
}
