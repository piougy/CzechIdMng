package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;

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
}
