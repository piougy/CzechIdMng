package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;

/**
 * Sms service
 * 
 * @author Peter Šourek
 */
public interface IdmSmsLogService extends 
		ReadWriteDtoService<IdmSmsLogDto, IdmNotificationFilter> {
}
