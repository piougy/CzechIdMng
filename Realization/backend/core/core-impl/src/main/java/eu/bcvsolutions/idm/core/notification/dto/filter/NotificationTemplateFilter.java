package eu.bcvsolutions.idm.core.notification.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Default notification template filter for using with service and manager,
 * that works with {@link IdmNotificationTemplate} entity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class NotificationTemplateFilter extends QuickFilter {
	
	private Boolean systemTemplate;

	public Boolean getSystemTemplate() {
		return systemTemplate;
	}

	public void setSystemTemplate(Boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}
	
}
