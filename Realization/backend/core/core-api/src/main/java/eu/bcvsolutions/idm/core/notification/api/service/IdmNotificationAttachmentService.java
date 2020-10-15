package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationAttachmentFilter;

/**
 * Notification attachment service.
 *
 * @author Radek Tomiška
 * @since 10.6.0 
 */
public interface IdmNotificationAttachmentService extends
		ReadWriteDtoService<IdmNotificationAttachmentDto, IdmNotificationAttachmentFilter> {

}
