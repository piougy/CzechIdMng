package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.service.NotificationService;

public abstract class AbstractNotificationService implements NotificationService {
	
	public static final String DEFAULT_TOPIC = "default";

	@Override
	public boolean send(IdmMessage message, IdmIdentity recipient) {
		return send(DEFAULT_TOPIC, message, recipient);
	}

	@Override
	public boolean send(String topic, IdmMessage message, IdmIdentity recipient) {
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		notification.setMessage(message);
		IdmNotificationRecipient notificationRecipient = new IdmNotificationRecipient(notification, recipient); 
		notification.getRecipients().add(notificationRecipient);
		return send(notification);
	}
	
	/**
	 * Clone notification message
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessage cloneMessage(IdmNotification notification) {
		return new IdmMessage(notification.getMessage().getSubject(), notification.getMessage().getTextMessage(), notification.getMessage().getHtmlMessage());
	}
	
	/**
	 * Clone recipients
	 * 
	 * @param notification - recipients new parent
	 * @param recipient - source recipient
	 * @return
	 */
	protected IdmNotificationRecipient cloneRecipient(IdmNotification notification, IdmNotificationRecipient recipient) {
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(), recipient.getRealRecipient());
	}
}
