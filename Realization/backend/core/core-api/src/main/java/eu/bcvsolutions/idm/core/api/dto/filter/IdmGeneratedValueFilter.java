package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;

/**
 * Generated value filter
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmGeneratedValueFilter extends DataFilter {

	public static final String PARAMETER_DISABLED = "disabled";
	public static final String PARAMETER_ENTITY_TYPE = "entityType";
	
	public IdmGeneratedValueFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmGeneratedValueFilter(MultiValueMap<String, Object> data) {
		super(IdmGeneratedValueDto.class, data);
	}

	public String getEntityType() {
		return (String) data.getFirst(PARAMETER_ENTITY_TYPE);
	}

	public void setEntityType(String entityType) {
		data.set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public Boolean getDisabled() {
		return (Boolean) data.getFirst(PARAMETER_DISABLED);
	}

	public void setDisabled(Boolean disabled) {
		data.set(PARAMETER_DISABLED, disabled);
	}
}
