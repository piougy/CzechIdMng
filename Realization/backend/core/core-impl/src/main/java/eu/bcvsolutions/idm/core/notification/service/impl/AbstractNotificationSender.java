package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationSender;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Basic notification service
 * 
 * @author Radek Tomiška
 * @author Ondřej Kopr
 *
 * @param <N> Notification type
 */
public abstract class AbstractNotificationSender<N extends IdmNotification> implements NotificationSender<N> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractNotificationSender.class);
	private final Class<N> notificationClass;
	
	@Autowired(required = false)
	private SecurityService securityService;
	
	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired(required = false)
	@Deprecated // will be removed after recipient refactoring
	private IdmIdentityService identityService;
	
	@SuppressWarnings("unchecked")
	public AbstractNotificationSender() {
		notificationClass = (Class<N>) GenericTypeResolver.resolveTypeArgument(getClass(), NotificationSender.class);
	}
	
	/**
	 * Returns true, if given delimiter equals this managers {@link IdmNotification} type.
	 */
	@Override
	public boolean supports(String delimiter) {
		String type = getType();
		if (StringUtils.isEmpty(type)) {
			return false;
		}
		return getType().equals(delimiter);
	}
	
	/**
	 * Returns this manager's {@link IdmNotification} type.
	 */
	@Override
	public String getType() {
		try {
			return notificationClass.newInstance().getType();
		} catch (InstantiationException | IllegalAccessException o_O) {
			LOG.error("[{}] does not supports a notification type. Fix notification service configuration or override supports method correctly.", notificationClass, o_O);
			return null;
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
	public N send(String topic, IdmMessage message) {
		Assert.notNull(securityService, "Security service is required for this operation");
		Assert.notNull(identityService, "Identity service is required for this operation");
		//
		IdmIdentityDto currentIdentityDto = securityService.getAuthentication().getCurrentIdentity();	
		if (currentIdentityDto == null || currentIdentityDto.getId() == null) {
			// system, guest, etc.
			return null;
		}
		IdmIdentity recipient = identityService.get(currentIdentityDto.getId());
		return send(topic, message, Lists.newArrayList(recipient));
	}

	@Override
	@Transactional
	public N send(String topic, IdmMessage message, List<IdmIdentity> recipients) {
		Assert.notNull(message, "Message is required");
		//
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		//
		notification.setMessage(message);
		// transform message parent
		recipients.forEach(recipient ->
			{
				notification.getRecipients().add(new IdmNotificationRecipient(notification, recipient));
			});
		// try to find template
		if (message.getTemplate() == null) {
			message.setTemplate(notificationTemplateService.resolveTemplate(notification.getTopic(), message.getLevel()));
		}
		notification.setMessage(this.notificationTemplateService.buildMessage(message, false));
		message = notification.getMessage(); // set build message back to message
		//
		// check if exist text for message, TODO: send only with subject?
		if (message.getHtmlMessage() == null && message.getSubject() == null && message.getTextMessage() == null && message.getModel() == null) {
			LOG.error("Notification has empty template and message. Message will not be send! [topic:{}]", topic);
			return null;
		}
		//
		return send(notification);
	}

	/**
	 * Clone notification message. Method just clone {@link IdmMessage}, or if object {@link IdmMessage}
	 * contain {@link IdmNotificationTemplate} it will be generate new message from templates and parameters if any.
	 * Clone message from template will not contain plain text of object {@link GuardedString}, just asterix.
	 * For show {@link GuardedString} in plain text use method for generate from template.
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessage cloneMessage(IdmNotification notification) {
		IdmMessage message = notification.getMessage();
		if (message.getTemplate() != null) {
			return this.getMessage(notification, false);
		}
		//
		message = new IdmMessage.Builder()
				.setLevel(message.getLevel())
				.setSubject(message.getSubject())
				.setTextMessage(message.getTextMessage())
				.setHtmlMessage(message.getHtmlMessage())
				.setModel(message.getModel())
				.setParameters(message.getParameters())
				.build();
		//
		// build message, message may contain some parameters
		return this.notificationTemplateService.buildMessage(message, false);
	}
	
	/**
	 * Return {@link IdmMessage} from notification, or generate new copy of {@link IdmMessage} from template. 
	 * {@link IdmMessage} is required. For generate {@link IdmMessage} from template is
	 * required object {@link IdmNotificationTemplate}
	 * Return {@link IdmMessage} or new generate instance from template.
	 * If not used generating from template, the parameter showGuardedString is irrelevant. Message is in plain text,
	 * it not possible to show or hide {@link GuardedString}
	 * 
	 * @param notification - notification that contain {@link IdmMessage} and {@ IdmNotificationTemplate}
	 * @param showGuardedString - flag for hide or show {@link GuardedString}
	 * @return
	 */
	protected IdmMessage getMessage(IdmNotification notification, boolean showGuardedString) {
		Assert.notNull(notification.getMessage());
		if (notification.getMessage().getTemplate() == null) {
			return notification.getMessage();
		}
		
		return this.notificationTemplateService.buildMessage(notification.getMessage(), showGuardedString);
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