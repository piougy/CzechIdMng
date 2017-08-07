package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;

/**
 * Email log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmEmailLogService extends 
		ReadWriteDtoService<IdmEmailLogDto, NotificationFilter> {

	/**
	 * Returns recipient's email address
	 *  
	 * @param recipient
	 * @return
	 */
	String getEmailAddress(IdmNotificationRecipientDto recipient);
	
	/**
	 * Returns identity's email address
	 *  
	 * @param identity
	 * @return
	 */
	String getEmailAddress(IdmIdentityDto identity);
	
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
