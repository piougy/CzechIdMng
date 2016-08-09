package eu.bcvsolutions.idm.core.notification.service.impl;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.service.EmailService;

@Component("emailService")
public class DefaultEmailService extends AbstractNotificationService implements EmailService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultEmailService.class);
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;
	
	@Autowired
    private ProducerTemplate producerTemplate;
	
	@Override
	public boolean send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		log.info("Adding email notification to queue [{}]", notification);
		IdmEmailLog emailLog = createLog(notification);		
		emailLog = emailLogRepository.save(emailLog);
		// send notification to routing
		producerTemplate.sendBody("direct:emails", emailLog);
		return true;
	}
	
	/**
	 * Returns recipient's email address, if identity's email is defined and realRecipient is defined, then realRecipient is returned (has higher priority)
	 *  
	 * @param recipient
	 * @return
	 */
	@Override
	public String getEmailAddress(IdmNotificationRecipient recipient) {
		if (recipient == null) {
			return null;
		}
		if (StringUtils.isNotBlank(recipient.getRealRecipient())) {
			return recipient.getRealRecipient();
		}
		if (recipient.getIdentityRecipient() != null && StringUtils.isNotBlank(recipient.getIdentityRecipient().getEmail())) {
			return recipient.getIdentityRecipient().getEmail();
		}
		return null;
	}
	
	@Override
	public void setEmailSent(Long emailLogId, Date sent) {
		IdmEmailLog emailLog = emailLogRepository.findOne(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		emailLog.setSent(sent);
		emailLogRepository.save(emailLog);
	}
	
	
	@Override
	public void setEmailSentLog(Long emailLogId, String sentLog) {
		IdmEmailLog emailLog = emailLogRepository.findOne(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		emailLog.setSentLog(sentLog);
		emailLogRepository.save(emailLog);
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
		emailLog.setParent(notification);
		// clone message
		emailLog.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		notification.getRecipients().forEach(recipient -> {
			emailLog.getRecipients().add(cloneRecipient(emailLog, recipient));
		});
		// clone from - resolve real email
		if (notification.getFrom() != null) {
			emailLog.setFrom(cloneRecipient(emailLog, notification.getFrom()));
		}
		return emailLogRepository.save(emailLog);
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
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(), getEmailAddress(recipient));
	}
}
