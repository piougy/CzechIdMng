package eu.bcvsolutions.idm.notification.service.impl;

import java.util.List;

import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.service.api.NotificationSender;

/**
 * Basic notification service
 * 
 * @author Radek Tomiška
 *
 * @param <N> Notification type
 */
public abstract class AbstractNotificationSender<N extends IdmNotification> implements NotificationSender<N> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractNotificationSender.class);
	private final Class<N> notificationClass;
	
	@SuppressWarnings("unchecked")
	public AbstractNotificationSender() {
		notificationClass = (Class<N>) GenericTypeResolver.resolveTypeArgument(getClass(), NotificationSender.class);
	}
	
	/**
	 * Returns true, if given delimiter equals this managers {@link IdmNotification} type.
	 */
	@Override
	public boolean supports(String delimiter) {
		try {
			return notificationClass.newInstance().getType().equals(delimiter);
		} catch (InstantiationException | IllegalAccessException o_O) {
			LOG.error("[{}] does not supports a notification type. Fix notification service configuration or override supports method correctly.", notificationClass, o_O);
			return false;
		}
	}
	
	@Override
	@Transactional
	public N send(IdmMessage message, IdmIdentity recipient) {
		return send(DEFAULT_TOPIC, message, recipient);
	}

	@Override
	@Transactional
	public N send(IdmMessage message, List<IdmIdentity> recipients) {
		return send(DEFAULT_TOPIC, message, recipients);
	}

	@Override
	@Transactional
	public N send(String topic, IdmMessage message, IdmIdentity recipient) {
		return send(topic, message, Lists.newArrayList(recipient));
	}

	@Override
	@Transactional
	public N send(String topic, IdmMessage message, List<IdmIdentity> recipients) {
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		notification.setMessage(message);
		recipients.forEach(recipient ->
			{
				notification.getRecipients().add(new IdmNotificationRecipient(notification, recipient));
			});
		return send(notification);
	}

	/**
	 * Clone notification message
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessage cloneMessage(IdmNotification notification) {
		return new IdmMessage(notification.getMessage().getSubject(), notification.getMessage().getTextMessage(),
				notification.getMessage().getHtmlMessage(), notification.getMessage().getParameters());
	}

	/**
	 * Clone recipients
	 * 
	 * @param notification
	 *            - recipients new parent
	 * @param recipient
	 *            - source recipient
	 * @return
	 */
	protected IdmNotificationRecipient cloneRecipient(IdmNotification notification,
			IdmNotificationRecipient recipient) {
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(),
				recipient.getRealRecipient());
	}
}
