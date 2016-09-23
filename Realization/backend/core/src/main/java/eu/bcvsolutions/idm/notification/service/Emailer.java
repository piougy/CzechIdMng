package eu.bcvsolutions.idm.notification.service;

import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;

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
	 * @param notification
	 * @return
	 */
	boolean send(IdmEmailLog emailLog);
	
}
