package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Persisted tokens
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class IdmTokenFilter extends DataFilter implements ExternalIdentifiable {
	
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_DISABLED = "disabled";
	public static final String PARAMETER_EXPIRATION_TILL = "expirationTill"; // expiration <= expirationTill
	
	public IdmTokenFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmTokenFilter(MultiValueMap<String, Object> data) {
        super(IdmTokenDto.class, data);
    }
    
    public void setOwnerId(UUID ownerId) {
    	data.set(PARAMETER_OWNER_ID, ownerId);
    }
    
    public UUID getOwnerId() {
    	return DtoUtils.toUuid(data.getFirst(PARAMETER_OWNER_ID));
    }
    
    public void setOwnerType(String ownerType) {
    	data.set(PARAMETER_OWNER_TYPE, ownerType);
    }
    
    public String getOwnerType() {
    	return (String) data.getFirst(PARAMETER_OWNER_TYPE);
    }
    
    public Boolean getDisabled() {
		// TODO: parameter converter
		Object disabled = data.getFirst(PARAMETER_DISABLED);
		if (disabled == null) {
			return null;
		}
		if (disabled instanceof Boolean) {
			return (Boolean) disabled;
		}
		return Boolean.valueOf(disabled.toString()) ;
	}

	public void setDisabled(Boolean disabled) {
		data.set(PARAMETER_DISABLED, disabled);
	}
	
	public ZonedDateTime getExpirationTill() {
		return getParameterConverter().toDateTime(data, PARAMETER_EXPIRATION_TILL);
	}
	
	public void setExpirationTill(ZonedDateTime expirationTill) {
		data.set(PARAMETER_EXPIRATION_TILL, expirationTill);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
}
