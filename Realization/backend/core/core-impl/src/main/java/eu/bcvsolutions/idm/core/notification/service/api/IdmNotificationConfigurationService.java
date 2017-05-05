package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;
import java.util.Set;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationService extends ReadWriteDtoService<NotificationConfigurationDto, IdmNotificationConfiguration, EmptyFilter> {

	/**
	 * Returns default notification sender. Will be used for notification's topics, which can not be found in configuration.
	 * 
	 * @return
	 */
	List<NotificationSender<?>> getDefaultSenders();
	
	/**
	 * Returns notification senders for given notification (by topic, level, etc).
	 * 
	 * @param notification
	 * @return
	 */
	List<NotificationSender<?>> getSenders(BaseNotification notification);
	
	/**
	 * Method return {@link IdmNotificationConfiguration} by topic, level, notification type.
	 * All parameters must exits. 
	 * @param topic
	 * @param level
	 * @return
	 */
	NotificationConfigurationDto getConfigurationByTopicLevelNotification(String topic, NotificationLevel level);
	
	/**
	 * Returns registered senders notification types.
	 * 
	 * @return
	 */
	Set<String> getSupportedNotificationTypes();
	
	/**
	 * Inits default notification configuration from all module descriptors.
	 */
	void initDefaultTopics();
}
