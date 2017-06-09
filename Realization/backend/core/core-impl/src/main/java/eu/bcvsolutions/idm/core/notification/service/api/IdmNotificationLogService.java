package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationLogService extends 
		ReadWriteDtoService<IdmNotificationLogDto, NotificationFilter>,
		AuthorizableService<IdmNotificationDto> {

    List<IdmNotificationRecipientDto> getRecipientsForNotification(String backendId);

}
