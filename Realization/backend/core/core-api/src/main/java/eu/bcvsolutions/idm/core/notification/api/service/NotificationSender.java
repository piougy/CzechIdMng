package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;

/**
 * Notification system. Notifications can be send to registered identities only. 
 * Sending notifications directly to email, phone etc. is not supported.
 * One notification can be send to more channels (e.g. email, websocket, sms). 
 * This is the reason why {@link #send(IdmMessageDto, List)} methods returns more than one notification.
 * 
 * @author Radek Tomiška 
 * 
 * @see {@link IdmNotificationLogDto}
 *
 */
public interface NotificationSender<N extends BaseNotification> extends Configurable, Plugin<String>, Ordered {
	
	String DEFAULT_TOPIC = "default";
	
	@Override
	default String getConfigurableType() {
		return "notification-sender";
	}
	
	/**
	 * Returns this sender's {@link IdmNotificationDto} type.
	 * 
	 * @return
	 */
	String getType();
	
	/**
	 * Returns this sender's {@link IdmNotificationDto} type.
	 * 
	 * @return
	 */
	@Override
	default String getName() {
		return getType();
	}
	
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
	List<N> send(IdmMessageDto message, IdmIdentityDto recipient);
	
	/**
	 * Sends given message to given identities.
	 * 
	 * @param message
	 * @param recipients
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	List<N> send(IdmMessageDto message, List<IdmIdentityDto> recipients);
	
	/**
	 * Sends given message with given topic to given identity.
	 * 
	 * @param topic
	 * @param message
	 * @param recipient
	 * @return sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	List<N> send(String topic, IdmMessageDto message, IdmIdentityDto recipient);
	
	/**
	 * Sends given message with given topic to given identities.
	 * 
	 * @param topic
	 * @param message
	 * @param recipients
	 * @return list of sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	List<N> send(String topic, IdmMessageDto message, List<IdmIdentityDto> recipients);
	
	/**
	 * Sends message with topic to given identities.
	 * 
	 * @param topic
	 * @param message
	 * @param sender [optional] notification from identity (e.g. admin sends notification to helpdesk identity)
	 * @param recipients
	 * @return list of sent IdmNotificationDto if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	List<N> send(String topic, IdmMessageDto message, IdmIdentityDto identitySender, List<IdmIdentityDto> recipients);
	
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
	List<N> send(String topic, IdmMessageDto message);
}