package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * For testing purpose only
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_notification_console")
public class IdmConsoleLog extends IdmNotification {
	
	private static final long serialVersionUID = -6492542811469689133L;
	public static final String NOTIFICATION_TYPE = "console";
	
	@Override
	public String getType() {
		return NOTIFICATION_TYPE;
	}
}
