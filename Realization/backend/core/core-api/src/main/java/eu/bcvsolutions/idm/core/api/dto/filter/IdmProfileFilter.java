package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;

/**
 * Filter for profiles.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmProfileFilter extends DataFilter {
	
	/**
	 * Identity
	 */
	public static final String PARAMETER_IDENTITY_ID = "identityId";
	
	public IdmProfileFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmProfileFilter(MultiValueMap<String, Object> data) {
		super(IdmProfileDto.class, data);
	}
	
	public UUID getIdentityId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_ID);
	}

	public void setIdentityId(UUID identityId) {
		set(PARAMETER_IDENTITY_ID, identityId);
	}

}
