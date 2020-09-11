package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;

/**
 * Notification recipient filter.
 * 
 * @author Radek Tomiška
 * @author Petr Šourek
 */
public class IdmNotificationRecipientFilter extends DataFilter {

    public static final String PARAMETER_NOTIFICATION = "notification"; 
    /**
     * Identity recipient.
     *
     * @since 10.6.0
     */
    public static final String PARAMETER_IDENTITY_RECIPIENT = "identityRecipient"; 
    /**
     * Real recipient - e.g. email.
     * 
     * @since 10.6.0
     */
    public static final String PARAMETER_REAL_RECIPIENT = "realRecipient";
	
	public IdmNotificationRecipientFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmNotificationRecipientFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmNotificationRecipientFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmNotificationRecipientDto.class, data, parameterConverter);
	}

    public UUID getNotification() {
        return getParameterConverter().toUuid(getData(), PARAMETER_NOTIFICATION);
    }

    public void setNotification(UUID notification) {
        set(PARAMETER_NOTIFICATION, notification);
    }

    /**
     * Identity recipient.
     * 
     * @return identity recipient
     * @since 10.6.0
     */
	public UUID getIdentityRecipient() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_RECIPIENT);
	}

	/**
	 * Identity recipient.
	 * 
	 * @param identityRecipient identity recipient.
	 * @since 10.6.0
	 */
	public void setIdentityRecipient(UUID identityRecipient) {
		set(PARAMETER_IDENTITY_RECIPIENT, identityRecipient);
	}

	/**
	 * Real recipient - e.g. email.
	 * 
	 * @return e.g. email
	 * @since 10.6.0
	 */
	public String getRealRecipient() {
		return getParameterConverter().toString(getData(), PARAMETER_REAL_RECIPIENT);
	}

	/**
	 * Real recipient - e.g. email.
	 * 
	 * @param realRecipient e.g. email
	 * @since 10.6.0
	 */
	public void setRealRecipient(String realRecipient) {
		set(PARAMETER_REAL_RECIPIENT, realRecipient);
	}
}
