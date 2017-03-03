package eu.bcvsolutions.idm.core.notification.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Excerpt projection for {@link IdmNotificationConfiguration}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Projection(name = "excerpt", types = IdmNotificationConfiguration.class)
public interface IdmNotificationConfigurationExcerpt extends AbstractDtoProjection {
	
	public String getTopic();

	public String getNotificationType();
	
	public NotificationLevel getLevel();

	public IdmNotificationTemplate getTemplate();

	public String getDescription();
}
