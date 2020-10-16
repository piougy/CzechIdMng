package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * Default notification template filter for using with service and manager,
 * that works with {@link IdmNotificationTemplate} entity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 *
 */
public class IdmNotificationTemplateFilter extends DataFilter {
	
	public static final String PARAMETER_UNMODIFIABLE = "unmodifiable";
	
    public IdmNotificationTemplateFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmNotificationTemplateFilter(MultiValueMap<String, Object> data) {
        super(IdmNotificationTemplateDto.class, data);
    }

	public Boolean getUnmodifiable() {
		return getParameterConverter().toBoolean(getData(),PARAMETER_UNMODIFIABLE);
	}

	public void setUnmodifiable(Boolean unmodifiable) {
		set(PARAMETER_UNMODIFIABLE, unmodifiable);
	}
}
