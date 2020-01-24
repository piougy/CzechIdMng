package eu.bcvsolutions.idm.core.notification.api.domain;

import org.springframework.http.HttpStatus;

/**
 * Common notification / operation / action level.
 * 
 * TODO: rename, move to core package level is used throuh application, not just for notification.
 * 
 * @author Radek Tomiška
 */
public enum NotificationLevel {

	SUCCESS,
	INFO,
	WARNING,
	ERROR; // ~danger
	
	/**
	 * Returns level for given http status
	 *  
	 * @param status
	 * @return
	 */
	public static NotificationLevel getLevel(HttpStatus status) {
		if (status.is5xxServerError()) {
            return NotificationLevel.ERROR;
        } 
		if (status.is2xxSuccessful()) {
            return NotificationLevel.SUCCESS;
        }
        return NotificationLevel.WARNING;
	}
}
