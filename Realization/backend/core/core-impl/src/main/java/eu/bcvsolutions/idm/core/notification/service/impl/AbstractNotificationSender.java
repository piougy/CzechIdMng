package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
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
public abstract class AbstractNotificationSender<N extends IdmNotificationDto> implements NotificationSender<N> {

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
	
	@Override
	@Transactional
	public N send(IdmMessageDto message, IdmIdentityDto recipient) {
		return send(DEFAULT_TOPIC, message, recipient);
	}

	@Override
	@Transactional
	public N send(IdmMessageDto message, List<IdmIdentityDto> recipients) {
		return send(DEFAULT_TOPIC, message, recipients);
	}

	@Override
	@Transactional
	public N send(String topic, IdmMessageDto message, IdmIdentityDto recipient) {
		return send(topic, message, Lists.newArrayList(recipient));
	}
	
	@Override
	@Transactional
	public N send(String topic, IdmMessageDto message) {
		Assert.notNull(securityService, "Security service is required for this operation");
		Assert.notNull(identityService, "Identity service is required for this operation");
		//
		IdmIdentityDto currentIdentityDto = securityService.getAuthentication().getCurrentIdentity();	
		if (currentIdentityDto == null || currentIdentityDto.getId() == null) {
			// system, guest, etc.
			return null;
		}

		return send(topic, message, Lists.newArrayList(currentIdentityDto));
	}

	@Override
	@Transactional
	public N send(String topic, IdmMessageDto message, List<IdmIdentityDto> recipients) {
		Assert.notNull(message, "Message is required");
		//
		IdmNotificationLogDto notification = new IdmNotificationLogDto();
		notification.setTopic(topic);
		//
		notification.setMessage(message);
		// transform message parent
		recipients.forEach(recipient ->
			{
				notification.getRecipients().add(new IdmNotificationRecipientDto(notification.getId(), recipient.getId()));
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
	 * Clone notification message. Method just clone {@link IdmMessageDto}, or if object {@link IdmMessageDto}
	 * contain {@link IdmNotificationTemplateDto} it will be generate new message from templates and parameters if any.
	 * Clone message from template will not contain plain text of object {@link GuardedString}, just asterisk.
	 * For show {@link GuardedString} in plain text use method for generate from template.
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessageDto cloneMessage(IdmNotificationDto notification) {
		IdmMessageDto message = notification.getMessage();
		if (message.getTemplate() != null) {
			return this.getMessage(notification, false);
		}
		//
		message = new IdmMessageDto(message);
		//
		// build message, message may contain some parameters
		return this.notificationTemplateService.buildMessage(message, false);
	}
	
	/**
	 * Return {@link IdmMessageDto} from notification, or generate new copy of {@link IdmMessageDto} from template.
	 * {@link IdmMessageDto} is required. For generate {@link IdmMessageDto} from template is
	 * required object {@link IdmNotificationTemplateDto}
	 * Return {@link IdmMessageDto} or new generate instance from template.
	 * If not used generating from template, the parameter showGuardedString is irrelevant. Message is in plain text,
	 * it not possible to show or hide {@link GuardedString}
	 * 
	 * @param notification - notification that contain {@link IdmMessageDto} and {@ IdmNotificationTemplateDto}
	 * @param showGuardedString - flag for hide or show {@link GuardedString}
	 * @return
	 */
	protected IdmMessageDto getMessage(IdmNotificationDto notification, boolean showGuardedString) {
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
	protected IdmNotificationRecipientDto cloneRecipient(IdmNotificationDto notification,
			IdmNotificationRecipientDto recipient) {
		return new IdmNotificationRecipientDto(notification.getId(), recipient.getIdentityRecipient(),
				recipient.getRealRecipient());
	}
}