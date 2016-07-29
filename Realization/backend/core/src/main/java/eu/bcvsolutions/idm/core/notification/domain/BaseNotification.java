package eu.bcvsolutions.idm.core.notification.domain;

import java.util.List;

import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;

/**
 * Common message properties for notification system
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface BaseNotification {

	void setFrom(IdmNotificationRecipient from);
	
	IdmNotificationRecipient getFrom();

	void setRecipients(List<IdmNotificationRecipient> recipients);
	
	List<IdmNotificationRecipient> getRecipients();
	
	void setMessage(IdmMessage message);
	
	IdmMessage getMessage();
}
