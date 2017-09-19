package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Default notification template filter for using with service and manager,
 * that works with {@link IdmNotificationTemplate} entity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmNotificationTemplateFilter extends QuickFilter {
	
	private Boolean unmodifiable;

	public Boolean getUnmodifiable() {
		return unmodifiable;
	}

	public void setUnmodifiable(Boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
}
