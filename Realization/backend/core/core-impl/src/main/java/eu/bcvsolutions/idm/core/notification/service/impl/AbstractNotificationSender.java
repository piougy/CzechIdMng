package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationSender;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
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

	@Autowired(required = false)
	private SecurityService securityService;
	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	private IdmNotificationConfigurationService notificationConfigurationService = null;
	
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
	public List<N> send(IdmMessageDto message, IdmIdentityDto recipient) {
		return send(DEFAULT_TOPIC, message, recipient);
	}

	@Override
	@Transactional
	public List<N> send(IdmMessageDto message, List<IdmIdentityDto> recipients) {
		return send(DEFAULT_TOPIC, message, recipients);
	}

	@Override
	@Transactional
	public List<N> send(String topic, IdmMessageDto message, IdmIdentityDto recipient) {
		return send(topic, message, Lists.newArrayList(recipient));
	}
	
	@Override
	@Transactional
	public List<N> send(String topic, IdmMessageDto message) {
		Assert.notNull(securityService, "Security service is required for this operation");
		Assert.notNull(topic, "Message topic can not be null.");
		Assert.notNull(message, "Message can not be null.");
		//
		AbstractAuthentication auth = securityService.getAuthentication();
		IdmIdentityDto currentIdentityDto = auth == null ? null : auth.getCurrentIdentity();
		if (currentIdentityDto == null || currentIdentityDto.getId() == null) {
			 LOG.warn("No identity is currently signed, swallowing the message: [{}], parameters: [{}].",
					 message.getTextMessage(), message.getParameters());
			// system, guest, etc.
			return null;
		}

		return send(topic, message, Lists.newArrayList(currentIdentityDto));
	}

	@Override
	@Transactional
	public List<N> send(String topic, IdmMessageDto message, List<IdmIdentityDto> recipients) {
		return send(topic, message, null, recipients);
	}
	
	@Override
	@Transactional
	public List<N> send(String topic, IdmMessageDto message, IdmIdentityDto identitySender, List<IdmIdentityDto> recipients) {
		Assert.notNull(message, "Message is required");
		List<N> sendMessages = new ArrayList<>();
		//
		autowireNotificationService();
		//
		if (isTopicDisabled(topic, message.getLevel())) {
			LOG.info("Notification for [topic:{}] not found or disabled. No message will be sent.", topic);
			return sendMessages;
		}
		//
		List<IdmNotificationRecipientDto> notificationRecipients = new ArrayList<>();
		recipients.forEach(recipient ->{
			notificationRecipients.add(new IdmNotificationRecipientDto(recipient.getId()));
	    	});
		//
		List<IdmNotificationLogDto> notifications = notificationTemplateService.prepareNotifications(topic, message);
		//
		if (notifications.isEmpty()) {
			LOG.info("Notification for [topic:{}] not found. Any message will not be sent.", topic);
			// no notifications found
			return sendMessages;
		}
		//
		// iterate over all prepared notifications, set recipients and send them
		for (IdmNotificationLogDto notification : notifications) {
			// if topic has more configurations and some of them could be disabled
			if (isNotificationDisabled(notification)) {
				continue;
			}
			//
			final IdmMessageDto notificationMessage = notification.getMessage();
			if (notificationMessage.getHtmlMessage() == null 
					&& notificationMessage.getSubject() == null 
					&& notificationMessage.getTextMessage() == null 
					&& notificationMessage.getModel() == null) {
				LOG.error("Notification has empty template and message. Message will not be sent! [topic:{}]", topic);
				continue;
			}
			notification.setRecipients(notificationRecipients);
			notification.setIdentitySender(identitySender == null ? null : identitySender.getId());
			//
			sendMessages.add(send(notification));
		}
		return sendMessages;
	}
	
	@Override
	public int getOrder() {
		return ConfigurationService.DEFAULT_ORDER;
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
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
	 * clone recipients with resolved real email address
	 *
	 * @param notification - recipients new parent
	 * @param recipient - source recipient
	 * @return Clone of recipient without specified identifier
	 */
	protected IdmNotificationRecipientDto cloneRecipient(IdmNotificationDto notification,
			IdmNotificationRecipientDto recipient, String realRecipient) {
		return new IdmNotificationRecipientDto(notification.getId(), recipient.getIdentityRecipient(),
			realRecipient);
	}
	
	/**
	 * Method check if all notification configurations are disabled for topic and level
	 * @param topic
	 * @param level
	 * @return
	 */
	private boolean isTopicDisabled(String topic, NotificationLevel level) {
		if (!notificationConfigurationService.getConfigurations(topic, level).isEmpty()) {
			List<NotificationConfigurationDto> configurations = notificationConfigurationService.getNotDisabledConfigurations(topic, null, level);
			if (configurations.isEmpty()) {
				return true;
			} 
		} else if (!notificationConfigurationService.getConfigurationsLevelIsNull(topic).isEmpty()) {
			List<NotificationConfigurationDto> configurations = notificationConfigurationService.getNotDisabledConfigurations(topic, null);
			if (configurations.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method check if notification configuration for this notification is disabled
	 * @param notification
	 * @return
	 */
	private boolean isNotificationDisabled(IdmNotificationLogDto notification) {
		if (!notificationConfigurationService.getConfigurations(notification.getTopic(), notification.getMessage().getLevel()).isEmpty()) {
			NotificationConfigurationDto notifConfiguration = notificationConfigurationService.getConfigurationByTopicLevelNotificationType(notification.getTopic(), notification.getMessage().getLevel(), notification.getType());
			if (notifConfiguration != null && notifConfiguration.isDisabled()) {
				return true;
			} 
		} else {
			List<NotificationConfigurationDto> notifConfiguration = notificationConfigurationService.getConfigurationsLevelIsNull(notification.getTopic());
			notifConfiguration = notifConfiguration.stream().filter(config -> config.getNotificationType().equals(notification.getType())).collect(Collectors.toList());
			if (notifConfiguration.iterator().hasNext()) {
				if (notifConfiguration.iterator().next().isDisabled())
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Methods helps autowire notification service
	 */
	private void autowireNotificationService() {
		if (this.notificationConfigurationService == null)
			this.notificationConfigurationService = AutowireHelper.getBean(IdmNotificationConfigurationService.class); 
	}
}