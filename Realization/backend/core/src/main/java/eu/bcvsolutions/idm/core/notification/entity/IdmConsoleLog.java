package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.bcvsolutions.idm.core.notification.service.ConsoleNotificationService;

/**
 * For testing purpose only
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
