package eu.bcvsolutions.idm.notification.service.impl;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.notification.service.EmailService;

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
		log.trace("Resolving email address to recipient [{}]", recipient);
		String emailAddress = null;
		try {
			if (recipient == null) {
				return emailAddress;
			}
			if (StringUtils.isNotBlank(recipient.getRealRecipient())) {
				emailAddress = recipient.getRealRecipient();
				return emailAddress;
			}
			
			if (recipient.getIdentityRecipient() != null) {
				String identityEmail = getEmailAddress(recipient.getIdentityRecipient());
				if(StringUtils.isNotBlank(identityEmail)) {
					emailAddress = identityEmail;
				}
			}			
			return emailAddress;
		} finally {
			log.trace("Resolved email address to recipient [{}] is [{}]", recipient, emailAddress);
		}
	}	
	
	@Override
	public String getEmailAddress(IdmIdentity recipient) {
		Assert.notNull(recipient, "Recipient has to be filled!");
		//
		// TODO: hook - resolve identity emails
		return recipient.getEmail();
	}
	
	/**
	 * Persists sent date to given emailLogId
	 * 
	 * @param emailLogId
	 * @param sent
	 */
	@Override
	public void setEmailSent(Long emailLogId, Date sent) {
		IdmEmailLog emailLog = emailLogRepository.findOne(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		log.debug("Persist sent date [{}] to emailLogId [{}]", sent, emailLogId);
		emailLog.setSent(sent);
		emailLogRepository.save(emailLog);
	}
	
	/**
	 * Persists sent log to given emailLog
	 * 
	 * @param emailLogId
	 * @param sentLog
	 */
	@Override
	public void setEmailSentLog(Long emailLogId, String sentLog) {
		IdmEmailLog emailLog = emailLogRepository.get(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		log.debug("Persist sent log [{}] to emailLogId [{}]", sentLog, emailLogId);
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
		if (notification.getId() != null) {
			emailLog.setParent(notification);
		}
		// clone message
		emailLog.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		notification.getRecipients().forEach(recipient -> {
			emailLog.getRecipients().add(cloneRecipient(emailLog, recipient));
		});
		emailLog.setSender(notification.getSender());
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
