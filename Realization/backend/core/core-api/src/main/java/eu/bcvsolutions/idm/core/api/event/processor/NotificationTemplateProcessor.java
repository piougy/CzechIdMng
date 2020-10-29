package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * Template processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public interface NotificationTemplateProcessor extends EntityEventProcessor<IdmNotificationTemplateDto> {
	
}
