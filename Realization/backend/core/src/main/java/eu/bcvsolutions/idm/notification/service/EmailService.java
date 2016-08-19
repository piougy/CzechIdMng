package eu.bcvsolutions.idm.notification.service;

import java.util.Date;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface EmailService extends NotificationService {
	
	public static final String NOTIFICATION_TYPE = "email";
	
	/**
	 * Returns recipient's email address
	 *  
	 * @param recipient
	 * @return
	 */
	String getEmailAddress(IdmNotificationRecipient recipient);
	
	/**
	 * Returns identity's email address
	 *  
	 * @param recipient
	 * @return
	 */
	String getEmailAddress(IdmIdentity identity);
	
	/**
	 * Persists sent date to given emailLogId
	 * 
	 * @param emailLogId
	 * @param sent
	 */
	void setEmailSent(Long emailLogId, Date sent);
	
	/**
	 * Persists sent log to given emailLog
	 * 
	 * @param emailLogId
	 * @param sentLog
	 */
	void setEmailSentLog(Long emailLogId, String sentLog);

}
