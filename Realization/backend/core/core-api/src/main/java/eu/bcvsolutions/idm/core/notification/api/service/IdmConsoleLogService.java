package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;

/**
 * Console log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConsoleLogService extends 
		ReadWriteDtoService<IdmConsoleLogDto, IdmNotificationFilter> {

}
