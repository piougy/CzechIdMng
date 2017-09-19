package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;

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
	 * Returns this manager's {@link IdmNotificationDto} type.
	 * 
	 * @return
	 */
	String getType();
	
	/**
	 * Returns notification log type (entity class).
	 * 
	 * @return
	 */
	Class<? extends BaseEntity> getNotificationType();

	/**
	 * Sends given message to given identity.
	 * 
	 * @param message
	 * @param recipient
	 * @return sent IdmNotificationDto or ex
	 */
	N send(IdmMessageDto message, IdmIdentityDto recipient);
	
	/**
	 * Sends given message to given identities.
	 * 
	 * @param message
	 * @param recipients
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmMessageDto message, List<IdmIdentityDto> recipients);
	
	/**
	 * Sends given message with given topic to given identity.
	 * 
	 * @param topic
	 * @param message
	 * @param recipient
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessageDto message, IdmIdentityDto recipient);
	
	/**
	 * Sends given message with given topic to given identities.
	 * 
	 * @param topic
	 * @param message
	 * @param recipients
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessageDto message, List<IdmIdentityDto> recipients);
	
	/**
	 * Sends given notification
	 * 
	 * @param notification
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmNotificationDto notification);
	
	/**
	 * Sends given message with given topic to currently logged identity.
	 * 
	 * @param topic
	 * @param message
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmMessageDto message);
}