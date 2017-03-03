package eu.bcvsolutions.idm.core.notification.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

@Projection(name = "excerpt", types = IdmNotificationTemplate.class)
public interface IdmNotificationTemplateExcerpt extends AbstractDtoProjection {
	
	public boolean isSystemTemplate();

	public String getBodyHtml();

	public String getBodyText();
	
	public String getName();

	public String getCode();

	public String getSubject();

	public String getParameter();
	
	public String getModule();
}
