package eu.bcvsolutions.idm.notification.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

@Projection(name = "excerpt", types = IdmNotificationLog.class)
public interface IdmNotificationLogExcerpt extends AbstractDtoProjection {
	

}
