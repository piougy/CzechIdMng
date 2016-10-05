package eu.bcvsolutions.idm.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.bcvsolutions.idm.notification.service.ConsoleNotificationService;

/**
 * For testing purpose only
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_console_log")
public class IdmConsoleLog extends IdmNotification {
	
	private static final long serialVersionUID = -6492542811469689133L;
	
	@Override
	public String getType() {
		return ConsoleNotificationService.NOTIFICATION_TYPE;
	}
}
