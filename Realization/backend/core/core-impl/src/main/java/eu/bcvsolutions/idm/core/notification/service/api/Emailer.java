package eu.bcvsolutions.idm.core.notification.service.api;

import org.springframework.transaction.event.TransactionalEventListener;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.domain.SendOperation;

/**
 * Email sender
 * 
 * @author Radek Tomiška 
 *
 */
public interface Emailer {
	
	/**
	 * When more emails is given, then single email is seperated by this separator
	 */
	static final String EMAILS_SEPARATOR = ",";

	/**
	 * Sends email by given notification
	 * 
	 * @param emailLog
	 * @return
	 */
	boolean send(IdmEmailLogDto emailLog);
	
	/**
	 * Internal send message. We need to wait to transaction commit - save email log
	 * 
	 * @param sendOperation
	 * @return
	 */
	@TransactionalEventListener
	public void sendInternal(SendOperation sendOperation);
	
}
