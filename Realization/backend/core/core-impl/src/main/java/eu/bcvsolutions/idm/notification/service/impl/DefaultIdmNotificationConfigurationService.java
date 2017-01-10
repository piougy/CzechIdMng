package eu.bcvsolutions.idm.notification.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.notification.domain.BaseNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.notification.service.api.NotificationSender;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmNotificationConfigurationService extends AbstractReadWriteEntityService<IdmNotificationConfiguration, EmptyFilter> implements IdmNotificationConfigurationService {
	
	private final IdmNotificationConfigurationRepository repository;
	private final PluginRegistry<NotificationSender<?>, String> notificationSenders;
	
	@Autowired
	public DefaultIdmNotificationConfigurationService(
			IdmNotificationConfigurationRepository repository,
			List<? extends NotificationSender<?>> notificationSenders) {
		super(repository);		
		//
		Assert.notEmpty(notificationSenders);
		//
		this.repository = repository;
		this.notificationSenders = OrderAwarePluginRegistry.create(notificationSenders);
	}
	
	@Override
	public List<NotificationSender<?>> getDefaultSenders() {
		List<NotificationSender<?>> senders = new ArrayList<>();
		senders.add(notificationSenders.getPluginFor("console")); // TODO: logger sender, configuration, nothing?
		return Collections.unmodifiableList(senders);
	}
	
	@Override
	public List<NotificationSender<?>> getSenders(BaseNotification notification) {
		Assert.notNull("notification");
		//
		String topic = notification.getTopic();
		if (StringUtils.isEmpty(notification.getTopic())) {
			return getDefaultSenders();
		}
		List<NotificationSender<?>> senders = new ArrayList<>();
		repository.findDistinctNotificationTypeByTopic(topic).forEach(type -> {
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

}
