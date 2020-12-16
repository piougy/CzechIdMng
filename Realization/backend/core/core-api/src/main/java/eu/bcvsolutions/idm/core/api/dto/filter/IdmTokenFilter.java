package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Persisted tokens.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class IdmTokenFilter 
		extends DataFilter 
		implements ExternalIdentifiableFilter, DisableableFilter {
	
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_EXPIRATION_TILL = "expirationTill"; // expiration <= expirationTill ~ invalid tokens
	public static final String PARAMETER_EXPIRATION_FROM = "expirationFrom"; // expiration >= expirationFrom ~ valid tokens
	
	public IdmTokenFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmTokenFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }
    
    public IdmTokenFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(IdmTokenDto.class, data, parameterConverter);
    }
    
    public void setOwnerId(UUID ownerId) {
    	set(PARAMETER_OWNER_ID, ownerId);
    }
    
    public UUID getOwnerId() {
    	return getParameterConverter().toUuid(getData(), PARAMETER_OWNER_ID);
    }
    
    public void setOwnerType(String ownerType) {
    	set(PARAMETER_OWNER_TYPE, ownerType);
    }
    
    public String getOwnerType() {
    	return getParameterConverter().toString(getData(), PARAMETER_OWNER_TYPE);
    }
	
    /**
	 * Tokens by expiration date - token.expiration <= expirationTill ~ invalid tokens.
	 * 
	 * @return expiration from
	 */
	public ZonedDateTime getExpirationTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_EXPIRATION_TILL);
	}
	
	/**
	 * Tokens by expiration date - token.expiration <= expirationTill ~ invalid tokens.
	 * @param expirationTill
	 */
	public void setExpirationTill(ZonedDateTime expirationTill) {
		set(PARAMETER_EXPIRATION_TILL, expirationTill);
	}
	
	/**
	 * Tokens by expiration date - token.expiration >= expirationFrom ~ valid tokens.
	 * 
	 * @return expiration from
	 * @since 10.7.0
	 */
	public ZonedDateTime getExpirationFrom() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_EXPIRATION_FROM);
	}
	
	/**
	 * Tokens by expiration date - token.expiration >= expirationFrom ~ valid tokens.
	 * 
	 * @param expirationFrom expiration from
	 * @since 10.7.0
	 */
	public void setExpirationFrom(ZonedDateTime expirationFrom) {
		set(PARAMETER_EXPIRATION_FROM, expirationFrom);
	}
}
