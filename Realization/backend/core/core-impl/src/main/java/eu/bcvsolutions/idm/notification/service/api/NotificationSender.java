package eu.bcvsolutions.idm.notification.service.api;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.domain.BaseNotification;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;

/**
 * Notification system
 *
 * TODO: move to core api (requires IdmIdentity refactoring - sender and recipient should be represented as uuid + username)
 * 
 * @author Radek Tomiška 
 *
 */
public interface NotificationSender<N extends BaseNotification> extends Plugin<String> {
	
	static final String DEFAULT_TOPIC = "default";

	/**
	 * Sends given message to given identity.
	 * 
	 * @param message
	 * @param recipient
	 * @return sent IdmNotification or ex
	 */
	N send(IdmMessage message, IdmIdentity recipient);
	
	/**
	 * Sends given message to given identities.
	 * 
	 * @param message
	 * @param recipients
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmMessage message, List<IdmIdentity> recipients);
	
	/**
	 * Sends given message with given topic to given identity.
	 * 
	 * @param topic
	 * @param message
	 * @param recipient
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessage message, IdmIdentity recipient);
	
	/**
	 * Sends given message with given topic to given identities.
	 * 
	 * @param topic
	 * @param message
	 * @param recipients
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessage message, List<IdmIdentity> recipients);
	
	/**
	 * Sends given notification
	 * 
	 * @param notification
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmNotification notification);
	
	/**
	 * Sends given message with given topic to currently logged identity.
	 * 
	 * @param topic
	 * @param message
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessage message);
}
