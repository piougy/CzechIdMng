package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmConsoleLog;

/**
 * Console log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConsoleLogService extends ReadWriteEntityService<IdmConsoleLog, NotificationFilter> {

}
