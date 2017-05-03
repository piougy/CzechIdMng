package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;

/**
 * Notification recipient service
 *
 * @author Peter Šourek
 */
public interface IdmNotificationRecipientService extends ReadWriteDtoService<IdmNotificationRecipientDto, IdmNotificationRecipient, NotificationRecipientFilter> {


}
