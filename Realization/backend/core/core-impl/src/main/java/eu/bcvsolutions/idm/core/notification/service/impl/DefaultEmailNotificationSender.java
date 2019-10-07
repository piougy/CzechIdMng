package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

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
    private final IdmNotificationTemplateService notificationTemplateService;
    
    @Autowired
    public DefaultEmailNotificationSender(
    		IdmEmailLogService emailLogService,
    		ProducerTemplate producerTemplate,
    		IdmNotificationTemplateService notificationTemplateService) {
    	//
    	Assert.notNull(emailLogService, "Service is required.");
    	Assert.notNull(producerTemplate, "Producer template is required.");
    	Assert.notNull(notificationTemplateService, "Service is required.");
    	//
		this.producerTemplate = producerTemplate;
		this.emailLogService = emailLogService;
		this.notificationTemplateService = notificationTemplateService;
	}
	
	@Override
	public String getType() {
		return IdmEmailLog.NOTIFICATION_TYPE;
	}
	
	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return emailLogService.getEntityClass();
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
	
	@Override
	@Transactional
	public IdmEmailLogDto send(IdmMessageDto message, String email) {
		Assert.notNull(email, "Email is required.");
		//
		return this.send(message, new String[]{ email });
	}
	
	@Override
	@Transactional
	public IdmEmailLogDto send(IdmMessageDto message, String[] emails) {
		return send(message, emails, null);
	}
	
	@Override
	@Transactional
	public IdmEmailLogDto send(IdmMessageDto message, String[] emails, List<IdmAttachmentDto> attachments) {
		Assert.notNull(message, "Message is required.");
		Assert.notNull(emails, "Emails are required.");
		//
		IdmEmailLogDto emailLog = new IdmEmailLogDto();
		// there is no parent
		// build message, without password
		emailLog.setMessage(notificationTemplateService.buildMessage(message, false));
		//
		for (String email : emails){
			// fill email to recipientDto
			emailLog.getRecipients().add(new IdmNotificationRecipientDto(email));
		}
		emailLog.setAttachments(attachments);
		emailLog = this.emailLogService.save(emailLog);
		// TODO: remove after attachments will be persisted
		emailLog.setAttachments(attachments);
		//
		producerTemplate.sendBody("direct:emails", emailLog);
		//
		return emailLog;
	}
	
	/**
	 * Persists new emailog record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmEmailLogDto createLog(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required.");
		Assert.notNull(notification.getMessage(), "Message is required.");
		//
		IdmEmailLogDto emailLog = new IdmEmailLogDto();
		// parent message
		if (notification.getId() != null) {
			emailLog.setParent(notification.getId());
		}
		// clone message
		emailLog.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		for(IdmNotificationRecipientDto recipient : notification.getRecipients()) {
			emailLog.getRecipients().add(cloneRecipient(emailLog, recipient));
		}
		emailLog.setIdentitySender(notification.getIdentitySender());
		emailLog.setType(IdmEmailLog.NOTIFICATION_TYPE);
		emailLog.setAttachments(notification.getAttachments());
		emailLog = emailLogService.save(emailLog);
		// TODO: remove after attachments will be persisted
		emailLog.setAttachments(notification.getAttachments());
		return emailLog;
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
