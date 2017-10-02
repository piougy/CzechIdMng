package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;

/**
 * Sends notification over all registered sender by notification configuration.
 * 
 * @author Radek Tomiška
 *
 */
public interface NotificationManager extends NotificationSender<IdmNotificationLogDto> {

}
