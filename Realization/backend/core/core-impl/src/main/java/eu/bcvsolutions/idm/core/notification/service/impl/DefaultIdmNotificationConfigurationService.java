package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationSender;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@Service("notificationConfigurationService")
public class DefaultIdmNotificationConfigurationService 
		extends AbstractReadWriteDtoService<NotificationConfigurationDto, IdmNotificationConfiguration, IdmNotificationConfigurationFilter>
		implements IdmNotificationConfigurationService {
	
	private final IdmNotificationConfigurationRepository repository;
	private final PluginRegistry<NotificationSender<?>, String> notificationSenders;
	private final ModuleService moduleService;
	
	@Autowired
	public DefaultIdmNotificationConfigurationService(
			IdmNotificationConfigurationRepository repository,
			List<? extends NotificationSender<?>> notificationSenders,
			ModuleService moduleService) {
		super(repository);		
		//
		Assert.notEmpty(notificationSenders);
		Assert.notNull(moduleService);
		//
		this.repository = repository;
		this.notificationSenders = OrderAwarePluginRegistry.create(notificationSenders);
		this.moduleService = moduleService;
	}
	
	@Override
	@Transactional
	public NotificationConfigurationDto save(NotificationConfigurationDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		//
		// check duplicity
		IdmNotificationConfiguration duplicitEntity = repository.findByTopicAndLevelAndNotificationType(dto.getTopic(), dto.getLevel(), dto.getNotificationType());
		if (duplicitEntity != null && !duplicitEntity.getId().equals(dto.getId())) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TOPIC_AND_LEVEL_EXISTS, ImmutableMap.of("topic", dto.getTopic()));
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
		if (!IdmNotificationLog.NOTIFICATION_TYPE.equals(notification.getType())) {
			if (notificationSenders.hasPluginFor(notification.getType())) {
				senders.add(notificationSenders.getPluginFor(notification.getType()));
			}
		} else {		
			final NotificationLevel lvl = notification.getMessage().getLevel();
			final List<String> types = repository.findTypes(topic, lvl);
			types.forEach(type -> {
				if (notificationSenders.hasPluginFor(type)) {
					senders.add(notificationSenders.getPluginFor(type));
				}
			});
		}
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
	public Class<? extends BaseEntity> toSenderType(String notificationType) {
		if (StringUtils.isEmpty(notificationType)) {
			return null;
		}
		for(NotificationSender<?> sender : notificationSenders.getPlugins()) {
			if (sender.getType().equals(notificationType)) {
				return sender.getNotificationType();
			}
		}
		throw new CoreException(String.format("Notification type [%s] is not supported.", notificationType));
	}

	@Override
	public NotificationConfigurationDto getConfigurationByTopicLevelNotificationType(String topic, NotificationLevel level, String notificationType) {
		return toDto(this.repository.findByTopicAndLevelAndNotificationType(topic, level, notificationType));
	}

	@Override
	public List<NotificationConfigurationDto> getConfigurations(String topic, NotificationLevel level) {
		return toDtos(repository.findByTopicAndLevel(topic, level), false);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmNotificationConfiguration> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmNotificationConfigurationFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//topic of notification configuration
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmNotificationConfiguration_.topic)),( "%" + filter.getText() + "%")));
		}
		//level of notification configuration
		if (filter.getLevel() != null) {
			predicates.add(builder.equal(root.get(IdmNotificationConfiguration_.level), filter.getLevel()));
		}
		//notification type of notification configuration
		if (StringUtils.isNotEmpty(filter.getNotificationType())) {
			predicates.add(builder.like(builder.lower(root.get(IdmNotificationConfiguration_.notificationType)), "%" + filter.getNotificationType() + "%"));
		}
		//template uuid of notification configuration
		if (filter.getTemplate() != null) {
			predicates.add(builder.equal(root.get(IdmNotificationConfiguration_.template).get(AbstractEntity_.id), filter.getTemplate()));
		}		
		//
		return predicates;
		}

}
