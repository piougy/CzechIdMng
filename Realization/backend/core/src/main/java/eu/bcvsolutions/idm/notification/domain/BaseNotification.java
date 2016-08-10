package eu.bcvsolutions.idm.notification.domain;

import java.util.List;

import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;

/**
 * Common message properties for notification system
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface BaseNotification {

	/**
	 * Notification type - email, notification, websocket etc.
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Notification sender - could be filled, when notification is send from
	 * some user
	 * 
	 * @param from
	 */
	void setFrom(IdmNotificationRecipient from);

	/**
	 * Notification sender - could be filled, when notification is send from
	 * some user
	 * 
	 * @return
	 */
	IdmNotificationRecipient getFrom();

	/**
	 * Notification recipients
	 * 
	 * @param recipients
	 */
	void setRecipients(List<IdmNotificationRecipient> recipients);

	/**
	 * Notification recipients
	 * 
	 * @return
	 */
	List<IdmNotificationRecipient> getRecipients();

	/**
	 * Sent message
	 * 
	 * @param message
	 */
	void setMessage(IdmMessage message);

	/**
	 * Sent message
	 * 
	 * @return
	 */
	IdmMessage getMessage();
}
