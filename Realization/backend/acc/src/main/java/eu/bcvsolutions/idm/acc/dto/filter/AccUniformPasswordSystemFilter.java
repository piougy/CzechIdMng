package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for connection between {@link AccUniformPasswordDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccUniformPasswordSystemFilter extends DataFilter {

	public static final String PARAMETER_UNIFORM_PASSWORD_ID = "uniformPasswordId";
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_IDENTITY_ID = "identityId";
	public static final String PARAMETER_UNIFORM_PASSWORD_DISABLED = "uniformPasswordDisabled";
	
	public AccUniformPasswordSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccUniformPasswordSystemFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public AccUniformPasswordSystemFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(AccUniformPasswordSystemDto.class, data, parameterConverter);
	}

	public UUID getUniformPasswordId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_UNIFORM_PASSWORD_ID);
	}

	public void setUniformPasswordId(UUID uniformPasswordId) {
		set(PARAMETER_UNIFORM_PASSWORD_ID, uniformPasswordId);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}

	public UUID getIdentityId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_ID);
	}

	public void setIdentityId(UUID identityId) {
		set(PARAMETER_IDENTITY_ID, identityId);
	}

	public Boolean getUniformPasswordDisabled() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_UNIFORM_PASSWORD_DISABLED);
	}

	public void setUniformPasswordDisabled(Boolean uniformPasswordDisabled) {
		set(PARAMETER_UNIFORM_PASSWORD_DISABLED, uniformPasswordDisabled);
	}
}
