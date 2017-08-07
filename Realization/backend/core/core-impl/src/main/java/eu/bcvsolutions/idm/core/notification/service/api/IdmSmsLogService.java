package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;

/**
 * Sms service
 * 
 * @author Peter Šourek
 */
public interface IdmSmsLogService extends 
		ReadWriteDtoService<IdmSmsLogDto, NotificationFilter> {
}
