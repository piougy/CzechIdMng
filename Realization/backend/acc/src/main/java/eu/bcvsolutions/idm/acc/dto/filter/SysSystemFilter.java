package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for systems.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class SysSystemFilter extends DataFilter {
	
	public static final String PARAMETER_VIRTUAL = "virtual";
	public static final String PARAMETER_PASSWORD_POLICY_VALIDATION_ID = "passwordPolicyValidationId";
	public static final String PARAMETER_PASSWORD_POLICY_GENERATION_ID = "passwordPolicyGenerationId";
	// Context parameters only
	public static final String PARAMETER_FILTER_SET_OUTSIDE_BE = "filterSetOutsideBE";
	
	public SysSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSystemFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemDto.class, data, parameterConverter);
	}

	public UUID getPasswordPolicyValidationId() {
		return getParameterConverter().toUuid(data, PARAMETER_PASSWORD_POLICY_VALIDATION_ID);
	}

	public void setPasswordPolicyValidationId(UUID passwordPolicyValidationId) {
		data.set(PARAMETER_PASSWORD_POLICY_VALIDATION_ID, passwordPolicyValidationId);
	}

	public UUID getPasswordPolicyGenerationId() {
		return getParameterConverter().toUuid(data, PARAMETER_PASSWORD_POLICY_GENERATION_ID);
	}

	public void setPasswordPolicyGenerationId(UUID passwordPolicyGenerationId) {
		data.set(PARAMETER_PASSWORD_POLICY_GENERATION_ID, passwordPolicyGenerationId);
	}

	public Boolean getVirtual() {
		return getParameterConverter().toBoolean(data, PARAMETER_VIRTUAL);
	}

	public void setVirtual(Boolean virtual) {
		data.set(PARAMETER_VIRTUAL, virtual);
	}

	public void setFilterSetFromOutsideBE(Boolean savePassword) {
		data.set(PARAMETER_FILTER_SET_OUTSIDE_BE, savePassword);
	}

	public Boolean isFilterSetOutsideBE() {
		return getParameterConverter().toBoolean(data, PARAMETER_FILTER_SET_OUTSIDE_BE);
	}
}
