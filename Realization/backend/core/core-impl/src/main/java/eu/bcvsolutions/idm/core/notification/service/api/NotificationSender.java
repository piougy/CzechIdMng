package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;
import java.util.Map;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.domain.BaseNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

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
	 * Returns this manager's {@link IdmNotification} type.
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Sends message by given template to given identity. Template message will be enhance by messageParameters.
	 * 
	 * @param message
	 * @param messageParameters - can be null, or empty
	 * @param recipient
	 * @return sent IdmNotification or ex
	 */
	N send(IdmNotificationTemplate template, Map<String, Object> messageParameters, IdmIdentity recipient);
	
	/**
	 * Sends message by given template to given identities. Template message will be enhance by messageParameters.
	 * 
	 * @param template
	 * @param messageParameters - can be null, or empty
	 * @param recipients
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmNotificationTemplate template, Map<String, Object> messageParameters, List<IdmIdentity> recipients);
	
	/**
	 * Sends message by given template with given topic to given identity. Template message will be enhance by messageParameters.
	 * 
	 * @param topic
	 * @param template
	 * @param messageParameters - can be null, or empty
	 * @param recipient
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters, IdmIdentity recipient);
	
	/**
	 * Sends message by given template with given topic to given identities. Template message will be enhance by messageParameters.
	 * 
	 * @param topic
	 * @param template
	 * @param messageParameters - can be null, or empty
	 * @param recipients
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters, List<IdmIdentity> recipients);
	
	/**
	 * Sends IdmNotification.
	 * 
	 * @param template
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(IdmNotification template);
	
	/**
	 * Sends message by given template with given topic to currently logged identity.
	 * 
	 * @param topic
	 * @param template
	 * @param messageParameters - can be null, or empty
	 * @return sent IdmNotification if notification was sent. Otherwise returns  null (not sent quietly) or ex (not sent and some error occurs).
	 */
	N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters);
	
	/**
	 * Send message from result model cot currently logged identity.
	 * IdmNotificationTempla will be temporary create.
	 * 
	 * @param topic
	 * @param model
	 * @return
	 */
	N send(String topic, ResultModel model);
}
