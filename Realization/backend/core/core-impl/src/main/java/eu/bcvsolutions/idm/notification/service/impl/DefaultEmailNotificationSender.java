package eu.bcvsolutions.idm.notification.service.impl;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.notification.service.api.IdmEmailLogService;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška
 *
 */
@Component("emailNotificationSender")
public class DefaultEmailNotificationSender extends AbstractNotificationSender<IdmEmailLog> implements EmailNotificationSender {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEmailNotificationSender.class);
	private final IdmEmailLogService emailLogService;
    private final ProducerTemplate producerTemplate;
    
    @Autowired
    public DefaultEmailNotificationSender(
    		IdmEmailLogService emailLogService,
    		ProducerTemplate producerTemplate) {
    	Assert.notNull(emailLogService);
    	Assert.notNull(producerTemplate);
    	//
		this.producerTemplate = producerTemplate;
		this.emailLogService = emailLogService;
	}
	
	@Override
	@Transactional
	public IdmEmailLog send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		LOG.info("Adding email notification to queue [{}]", notification);
		IdmEmailLog log = createLog(notification);
		// send notification to routing
		producerTemplate.sendBody("direct:emails", log);
		return log;
	}
	
	/**
	 * Persists new emailog record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmEmailLog createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		IdmEmailLog emailLog = new IdmEmailLog();
		// parent message
		if (notification.getId() != null) {
			emailLog.setParent(notification);
		}
		// clone message
		emailLog.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		notification.getRecipients().forEach(recipient -> {
			emailLog.getRecipients().add(cloneRecipient(emailLog, recipient));
		});
		emailLog.setIdentitySender(notification.getIdentitySender());
		return emailLogService.save(emailLog);
	}
	
	/**
	 * clone recipients with resolved real email address
	 * 
	 * @param notification - recipients new parent
	 * @param recipient - source recipient
	 * @return
	 */
	@Override
	protected IdmNotificationRecipient cloneRecipient(IdmNotification notification, IdmNotificationRecipient recipient) {
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(), emailLogService.getEmailAddress(recipient));
	}
}
