package eu.bcvsolutions.idm.notification.service.impl;

import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.notification.service.api.NotificationLogService;

/**
 * Sends notifications
 * 
 * @author Radek Tomiška
 *
 */
@Component("notificationService")
public class DefaultNotificationService extends AbstractNotificationService<IdmNotificationLog> implements NotificationLogService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultNotificationService.class);
    private final ProducerTemplate producerTemplate;
	
    @Autowired
	public DefaultNotificationService(
			IdmNotificationLogRepository repository,
			ProducerTemplate producerTemplate
			) {
    	super(repository);
    	//
    	Assert.notNull(producerTemplate);
    	//
    	this.producerTemplate = producerTemplate;
	}
	
	@Override
	@Transactional
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
		LOG.info("Sending notification [{}]", notificationLog);
		// send notification to routing
		producerTemplate.sendBody("direct:notifications", notificationLog);
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
		// we can only create log, if notification is instance of IdmNotificationLog
		if (notification instanceof IdmNotificationLog) {
			notification.setSent(new DateTime());
			return save((IdmNotificationLog) notification);
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
		notificationLog.setSender(notification.getSender());
		return save(notificationLog);
	}
}
