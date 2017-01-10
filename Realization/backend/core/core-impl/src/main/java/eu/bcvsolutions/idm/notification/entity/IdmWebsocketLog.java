package eu.bcvsolutions.idm.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Log for messages sent through websocket
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_notification_websocket")
public class IdmWebsocketLog extends IdmNotification {

	private static final long serialVersionUID = 2144902485645685319L;
	public static final String NOTIFICATION_TYPE = "websocket";

	@Override
	public String getType() {
		return NOTIFICATION_TYPE;
	}

}
