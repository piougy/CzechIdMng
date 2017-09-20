package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationLogService extends 
		ReadWriteDtoService<IdmNotificationLogDto, IdmNotificationFilter> {

    List<IdmNotificationRecipientDto> getRecipientsForNotification(String backendId);

}
