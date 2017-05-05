package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationSender;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmNotificationConfigurationService 
		extends AbstractReadWriteDtoService<NotificationConfigurationDto, IdmNotificationConfiguration, EmptyFilter>
		implements IdmNotificationConfigurationService {
	
	private final IdmNotificationConfigurationRepository repository;
	private final PluginRegistry<NotificationSender<?>, String> notificationSenders;
	private final ModuleService moduleService;
	private final IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired
	public DefaultIdmNotificationConfigurationService(
			IdmNotificationConfigurationRepository repository,
			List<? extends NotificationSender<?>> notificationSenders,
			ModuleService moduleService,
			IdmNotificationTemplateService notificationTemplateService) {
		super(repository);		
		//
		Assert.notEmpty(notificationSenders);
		Assert.notNull(moduleService);
		Assert.notNull(notificationTemplateService);
		//
		this.repository = repository;
		this.notificationSenders = OrderAwarePluginRegistry.create(notificationSenders);
		this.moduleService = moduleService;
		this.notificationTemplateService = notificationTemplateService;
	}
	
	@Override
	public NotificationConfigurationDto save(NotificationConfigurationDto dto, BasePermission... permission) {
		// create wild card, check if exist any another
		if (dto.getLevel() == null) {
			IdmNotificationConfiguration wildcard = repository.findNotificationByTopicLevel(dto.getTopic(), null);
			if (wildcard != null && !wildcard.equals(dto)) {
				throw new ResultCodeException(CoreResultCode.NOTIFICATION_TOPIC_AND_LEVEL_EXISTS, ImmutableMap.of("topic", dto.getTopic()));
			}
		}
		return super.save(dto);
	}
	
	/**
	 * Inits default notification configuration from all module descriptors.
	 */
	@Override
	@Transactional
	public void initDefaultTopics() {
		moduleService.getInstalledModules().forEach(module -> {
			Set<String> topicToCreate = new HashSet<>();
			module.getDefaultNotificationConfigurations().forEach(config -> {
				String topic = config.getTopic();
				Long count = repository.countByTopic(topic);
				if (topicToCreate.contains(topic) || count == 0) {
					topicToCreate.add(topic);
					UUID template = config.getTemplate();
					NotificationConfigurationDto notConfiguration = new NotificationConfigurationDto(config);
					notConfiguration.setTemplate(template);
					repository.save(toEntity(notConfiguration, null));
				}
			});
		});
	}
	
	@Override
	public List<NotificationSender<?>> getDefaultSenders() {
		List<NotificationSender<?>> senders = new ArrayList<>();
		senders.add(notificationSenders.getPluginFor("console")); // TODO: logger sender, configuration, nothing?
		return Collections.unmodifiableList(senders);
	}
	
	@Override
	public List<NotificationSender<?>> getSenders(BaseNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		String topic = notification.getTopic();
		if (StringUtils.isEmpty(notification.getTopic())) {
			return getDefaultSenders();
		}
		List<NotificationSender<?>> senders = new ArrayList<>();
		final NotificationLevel lvl = notification.getMessage().getLevel();
		final List<String> types = repository.findTypes(topic, lvl);
		types.forEach(type -> {
			if (notificationSenders.hasPluginFor(type)) {
				senders.add(notificationSenders.getPluginFor(type));
			}
		});
		//
		if (senders.isEmpty()) {
			return getDefaultSenders();
		}
		return senders;
	}
	
	@Override
	public Set<String> getSupportedNotificationTypes() {
		Set<String> types = new HashSet<>();
		notificationSenders.getPlugins().forEach(sender -> {
			String type = sender.getType();
			if (!IdmNotificationLog.NOTIFICATION_TYPE.equals(type)) { // we does not want NotificationManager's type (just notification envelope).
				types.add(sender.getType());
			}
		});
		return types;
	}

	@Override
	public NotificationConfigurationDto getConfigurationByTopicLevelNotification(String topic, NotificationLevel level) {
		final IdmNotificationConfiguration entity = this.repository.findNotificationByTopicLevel(topic, level);
		return toDto(entity);
	}

}
