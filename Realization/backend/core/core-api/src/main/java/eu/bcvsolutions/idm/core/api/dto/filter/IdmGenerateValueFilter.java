package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;

/**
 * Generated value filter
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmGenerateValueFilter extends DataFilter {

	public static final String PARAMETER_DISABLED = "disabled";
	public static final String PARAMETER_DTO_TYPE = "dtoType";
	
	public IdmGenerateValueFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmGenerateValueFilter(MultiValueMap<String, Object> data) {
		super(IdmGenerateValueDto.class, data);
	}

	public String getDtoType() {
		return (String) data.getFirst(PARAMETER_DTO_TYPE);
	}

	public void setDtoType(String dtoType) {
		data.set(PARAMETER_DTO_TYPE, dtoType);
	}

	public Boolean getDisabled() {
		return getParameterConverter().toBoolean(data, PARAMETER_DISABLED) ;
	}

	public void setDisabled(Boolean disabled) {
		data.set(PARAMETER_DISABLED, disabled);
	}
}
