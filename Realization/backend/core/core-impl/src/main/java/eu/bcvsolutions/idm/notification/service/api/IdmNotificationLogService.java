package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Notification logs
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationLogService extends ReadWriteEntityService<IdmNotificationLog, NotificationFilter> {

}
