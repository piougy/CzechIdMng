package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Uniform password filter
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccUniformPasswordFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_SYSTEM_ID = "systemId";

	public AccUniformPasswordFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccUniformPasswordFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public AccUniformPasswordFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(AccUniformPasswordDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}

}
