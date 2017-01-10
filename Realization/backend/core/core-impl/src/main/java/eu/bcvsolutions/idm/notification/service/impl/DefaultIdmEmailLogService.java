package eu.bcvsolutions.idm.notification.service.impl;

import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.notification.service.api.IdmEmailLogService;

/**
 * Email log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmEmailLogService extends AbstractReadWriteEntityService<IdmEmailLog, NotificationFilter> implements IdmEmailLogService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmEmailLogService.class);
	
	@Autowired
	public DefaultIdmEmailLogService(IdmEmailLogRepository repository) {
		super(repository);		
	}
	
	/**
	 * Returns recipient's email address, if identity's email is defined and realRecipient is defined, then realRecipient is returned (has higher priority)
	 *  
	 * @param recipient
	 * @return
	 */
	@Override
	public String getEmailAddress(IdmNotificationRecipient recipient) {
		LOG.trace("Resolving email address to recipient [{}]", recipient);
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
			LOG.trace("Resolved email address to recipient [{}] is [{}]", recipient, emailAddress);
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
	@Transactional
	public void setEmailSent(UUID emailLogId, DateTime sent) {
		IdmEmailLog emailLog = get(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		LOG.debug("Persist sent date [{}] to emailLogId [{}]", sent, emailLogId);
		emailLog.setSent(sent);
		save(emailLog);
	}
	
	/**
	 * Persists sent log to given emailLog
	 * 
	 * @param emailLogId
	 * @param sentLog
	 */
	@Override
	@Transactional
	public void setEmailSentLog(UUID emailLogId, String sentLog) {
		IdmEmailLog emailLog = get(emailLogId);
		Assert.notNull(emailLog, MessageFormat.format("Email log [id:{0}] does not exist", emailLogId));
		//
		LOG.debug("Persist sent log [{}] to emailLogId [{}]", sentLog, emailLogId);
		emailLog.setSentLog(sentLog);
		save(emailLog);
	}

}
