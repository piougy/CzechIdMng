package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationSender;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

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
	public N send(IdmNotificationTemplate template, Map<String, Object> messageParameters, IdmIdentity recipient) {
		return send(DEFAULT_TOPIC, template, messageParameters, recipient);
	}

	@Override
	@Transactional
	public N send(IdmNotificationTemplate template, Map<String, Object> messageParameters, List<IdmIdentity> recipients) {
		return send(DEFAULT_TOPIC, template, messageParameters, recipients);
	}

	@Override
	@Transactional
	public N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters, IdmIdentity recipient) {
		return send(topic, template, messageParameters, Lists.newArrayList(recipient));
	}
	
	@Override
	@Transactional
	public N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters) {
		Assert.notNull(securityService, "Security service is required for this operation");
		Assert.notNull(identityService, "Identity service is required for this operation");
		//
		IdentityDto currentIdentityDto = securityService.getAuthentication().getCurrentIdentity();	
		if (currentIdentityDto == null || currentIdentityDto.getId() == null) {
			// system, guest, etc.
			return null;
		}
		IdmIdentity recipient = identityService.get(currentIdentityDto.getId());
		return send(topic, template, messageParameters, Lists.newArrayList(recipient));
	}

	@Override
	@Transactional
	public N send(String topic, IdmNotificationTemplate template, Map<String, Object> messageParameters, List<IdmIdentity> recipients) {
		Assert.notNull(template, "Template is required");
		//
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		notification.setMessage(notificationTemplateService.getMessage(template));
		recipients.forEach(recipient ->
			{
				notification.getRecipients().add(new IdmNotificationRecipient(notification, recipient));
			});
		return send(notification);
	}
	
	@Override
	@Transactional
	public N send(String topic, ResultModel model) {
		Assert.notNull(model);
		NotificationLevel level;
		if (model.getStatus().is5xxServerError()) {
			level = NotificationLevel.ERROR;
		} else if(model.getStatus().is2xxSuccessful()) {
			level = NotificationLevel.SUCCESS;
		} else {
			level = NotificationLevel.WARNING;
		}
		//
		IdmMessage message = new IdmMessage.Builder()
				.setMessage(model.getMessage())
				.setLevel(level)
				.setModel(model)
				.setSubject(model.getStatusEnum())
				.build();
		//
		IdentityDto currentIdentityDto = securityService.getAuthentication().getCurrentIdentity();	
		if (currentIdentityDto == null || currentIdentityDto.getId() == null) {
			// system, guest, etc.
			return null;
		}
		IdmIdentity recipient = identityService.get(currentIdentityDto.getId());
		//
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		notification.setMessage(message);
		notification.getRecipients().add(new IdmNotificationRecipient(notification, recipient));
		//
		return send(notification);
	}

	/**
	 * Clone notification message
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessage cloneMessage(IdmNotification notification) {
		IdmMessage message = notification.getMessage();
		return new IdmMessage.Builder()
				.setLevel(message.getLevel())
				.setSubject(message.getSubject())
				.setTextMessage(message.getTextMessage())
				.setHtmlMessage(message.getHtmlMessage())
				.setModel(message.getModel())
				.build();
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
