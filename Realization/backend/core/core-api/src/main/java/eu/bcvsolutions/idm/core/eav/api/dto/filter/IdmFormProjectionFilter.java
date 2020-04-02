package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;

/**
 * Filter for form projection. Projections are found by owner (~entity) type mainly.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class IdmFormProjectionFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	
	public IdmFormProjectionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmFormProjectionFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmFormProjectionFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmFormProjectionDto.class, data, parameterConverter);
	}

	public String getOwnerType() {
		return getParameterConverter().toString(getData(), PARAMETER_OWNER_TYPE);
	}

	public void setOwnerType(String ownerType) {
		set(PARAMETER_OWNER_TYPE, ownerType);
	}
	
}