package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationAttachmentDto;

/**
 * Notification attachment filter.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public class IdmNotificationAttachmentFilter extends DataFilter {

    public static final String PARAMETER_NOTIFICATION = "notification"; 
	
	public IdmNotificationAttachmentFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmNotificationAttachmentFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmNotificationAttachmentFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmNotificationAttachmentDto.class, data, parameterConverter);
	}

    public UUID getNotification() {
        return getParameterConverter().toUuid(getData(), PARAMETER_NOTIFICATION);
    }

    public void setNotification(UUID notification) {
        set(PARAMETER_NOTIFICATION, notification);
    }
}
