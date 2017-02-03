package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;
import java.util.Set;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.domain.BaseNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationService extends ReadWriteEntityService<IdmNotificationConfiguration, EmptyFilter> {

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
