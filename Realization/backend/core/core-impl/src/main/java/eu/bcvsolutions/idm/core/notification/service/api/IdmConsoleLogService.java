package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;

/**
 * Console log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConsoleLogService extends ReadWriteDtoService<IdmConsoleLogDto, IdmConsoleLog, NotificationFilter> {

}
