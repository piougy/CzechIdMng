package eu.bcvsolutions.idm.notification.service.api;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;

/**
 * Email log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmEmailLogService extends ReadWriteEntityService<IdmEmailLog, NotificationFilter> {

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
	void setEmailSent(UUID emailLogId, DateTime sent);
	
	/**
	 * Persists sent log to given emailLog
	 * 
	 * @param emailLogId
	 * @param sentLog
	 */
	void setEmailSentLog(UUID emailLogId, String sentLog);
}
