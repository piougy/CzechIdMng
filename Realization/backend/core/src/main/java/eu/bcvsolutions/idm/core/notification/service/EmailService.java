package eu.bcvsolutions.idm.core.notification.service;

import java.util.Date;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;

public interface EmailService extends NotificationService {
	
	public static final String NOTIFICATION_TYPE = "email";
	
	/**
	 * Returns recipient's email address
	 *  
	 * @param recipient
	 * @return
	 */
	String getEmailAddress(IdmNotificationRecipient recipient);
	
	void setEmailSent(Long emailLogId, Date sent);
	
	void setEmailSentLog(Long emailLogId, String sentLog);

}
