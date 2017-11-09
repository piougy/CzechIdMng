package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;
import java.util.Set;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationConfigurationDto;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationService extends ReadWriteDtoService<IdmNotificationConfigurationDto, IdmNotificationConfigurationFilter> {

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
	 * Method return {@link IdmNotificationConfigurationDto} by topic, level, notification type.
	 * All parameters must exits, except notification level. 
	 * 
	 * @param topic
	 * @param level
	 * @param notificationType
	 * @return
	 */
	IdmNotificationConfigurationDto getConfigurationByTopicLevelNotificationType(String topic, NotificationLevel level, String notificationType);
	
	/**
	 * Returns registered senders notification types.
	 * 
	 * @return
	 */
	Set<String> getSupportedNotificationTypes();
	
	/**
	 * Returns notification log type from senders notification type.
	 * 
	 * @param notificationType
	 * @return
	 */
	Class<? extends BaseEntity> toSenderType(String notificationType);
	
	/**
	 * Inits default notification configuration from all module descriptors.
	 */
	void initDefaultTopics();
	
	/**
	 * Method find all match configurations for topic and level.
	 * 
	 * @param topic
	 * @param level
	 * @return
	 */
	List<IdmNotificationConfigurationDto> getConfigurations(String topic, NotificationLevel level);
}
