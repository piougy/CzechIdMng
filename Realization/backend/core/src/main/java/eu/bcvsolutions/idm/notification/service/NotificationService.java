package eu.bcvsolutions.idm.notification.service;

import java.util.List;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;

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
	 * Sends given message with given topic to given identities.
	 * 
	 * @param topic
	 * @param message
	 * @param recipient
	 * @return
	 */
	boolean send(String topic, IdmMessage message, List<IdmIdentity> recipients);
	
	/**
	 * Sends given notification
	 * 
	 * @param notification
	 * @return
	 */
	boolean send(IdmNotification notification);
}
