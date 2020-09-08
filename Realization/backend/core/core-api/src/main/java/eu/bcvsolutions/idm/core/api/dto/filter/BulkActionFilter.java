package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Bulk action filter.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public class BulkActionFilter extends DataFilter {

	public static final String PARAMETER_ENTITY_CLASS = "entityClass";  // equals
	public static final String PARAMETER_NAME = "name";  // equals
	public static final String PARAMETER_MODULE = "module"; // equals
	public static final String PARAMETER_DESCRIPTION  = "description"; // like	

	public BulkActionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public BulkActionFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public BulkActionFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(IdmBulkActionDto.class , data, parameterConverter);
    }

	public String getName() {
		return getParameterConverter().toString(getData(), PARAMETER_NAME);
	}

	public void setName(String name) {
		set(PARAMETER_NAME, name);
	}

	public String getDescription() {
		return getParameterConverter().toString(getData(), PARAMETER_DESCRIPTION);
	}

	public void setDescription(String description) {
		set(PARAMETER_DESCRIPTION, description);
	}

	public String getModule() {
		return getParameterConverter().toString(getData(), PARAMETER_MODULE);
	}

	public void setModule(String module) {
		set(PARAMETER_MODULE, module);
	}

	public String getEntityClass() {
		return getParameterConverter().toString(getData(), PARAMETER_ENTITY_CLASS);
	}

	public void setEntityClass(String entityClass) {
		set(PARAMETER_ENTITY_CLASS, entityClass);
	}
}
