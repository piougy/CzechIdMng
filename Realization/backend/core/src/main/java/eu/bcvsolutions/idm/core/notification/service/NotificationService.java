package eu.bcvsolutions.idm.core.notification.service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;

/**
 * Notification system
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface NotificationService {

	/**
	 * Sends given message to given identity.
	 * 
	 * @param message
	 * @param recipient
	 * @return
	 */
	boolean send(IdmMessage message, IdmIdentity recipient);
	
	/**
	 * Sends given message with given topic to given identity.
	 * 
	 * @param topic
	 * @param message
	 * @param recipient
	 * @return
	 */
	boolean send(String topic, IdmMessage message, IdmIdentity recipient);
	
	/**
	 * Sends given notification
	 * 
	 * @param notification
	 * @return
	 */
	boolean send(IdmNotification notification);
}
