package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.Date;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.NotificationLogService;

@Component("notificationService")
public class DefaultNotificationService extends AbstractNotificationService implements NotificationLogService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractNotificationService.class);
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
    private ProducerTemplate producerTemplate;
	
	@Override
	public boolean send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
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
	@Override
	public boolean sendNotificationLog(IdmNotificationLog notificationLog) {
		log.info("Sending notification [{}]", notificationLog);
		// send notification to routing
		producerTemplate.sendBody("direct:notifications", notificationLog);
		return true;
	}
	
	/**
	 * Persists new emailog record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotification createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		IdmNotificationLog notificationLog = new IdmNotificationLog();
		notificationLog.setSent(new Date());
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients().add(cloneRecipient(notificationLog, recipient));
		});
		// clone from - resolve real email
		if (notification.getFrom() != null) {
			notificationLog.setFrom(cloneRecipient(notificationLog, notification.getFrom()));
		}
		return idmNotificationRepository.save(notificationLog);
	}
}
