package eu.bcvsolutions.idm.core.notification.service.impl;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.service.api.IdmEmailLogService;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška
 *
 */
@Component("emailNotificationSender")
public class DefaultEmailNotificationSender extends AbstractNotificationSender<IdmEmailLogDto> implements EmailNotificationSender {

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
	public String getType() {
		return IdmEmailLog.NOTIFICATION_TYPE;
	}

	@Override
	@Transactional
	public IdmEmailLogDto send(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		LOG.info("Adding email notification to queue [{}]", notification);
		IdmEmailLogDto log = createLog(notification);
		// send notification to routing, generate new message
		producerTemplate.sendBody("direct:emails", log);
		return log;
	}
	
	/**
	 * Persists new emailog record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmEmailLogDto createLog(IdmNotificationDto notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		IdmEmailLogDto emailLog = new IdmEmailLogDto();
		// parent message
		if (notification.getId() != null) {
			emailLog.setParent(notification.getId());
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
	protected IdmNotificationRecipientDto cloneRecipient(IdmNotificationDto notification, IdmNotificationRecipientDto recipient) {
		return new IdmNotificationRecipientDto(notification.getId(), recipient.getIdentityRecipient(), emailLogService.getEmailAddress(recipient));
	}
}
