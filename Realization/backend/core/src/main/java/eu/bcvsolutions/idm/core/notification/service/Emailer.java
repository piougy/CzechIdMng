package eu.bcvsolutions.idm.core.notification.service;

import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

/**
 * Email sender
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
	 * @param notification
	 * @return
	 */
	boolean send(IdmEmailLog emailLog);
	
}
