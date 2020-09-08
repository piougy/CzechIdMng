package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter builder filter.
 *
 * @author Kolychev Artem
 * @author Radek Tomiška
 * @since 9.7.7
 */
public class FilterBuilderFilter extends DataFilter {

    public static final String PARAMETER_NAME = "name"; // equals
    public static final String PARAMETER_MODULE = "module"; // equals
    public static final String PARAMETER_DESCRIPTION = "description"; // like
    public static final String PARAMETER_FILTER_BUILDER_CLASS = "filterBuilderClass"; // equals (use text for like)
    public static final String PARAMETER_ENTITY_CLASS = "entityClass"; // equals (use text for like)

    public FilterBuilderFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public FilterBuilderFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }

    public FilterBuilderFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(FilterBuilderDto.class , data, parameterConverter);
    }

    public String getName() {
        return getParameterConverter().toString(data, PARAMETER_NAME);
    }

    public void setName(String name) {
        data.set(PARAMETER_NAME, name);
    }

    public String getModule() {
        return getParameterConverter().toString(data, PARAMETER_MODULE);
    }

    public void setModule(String module) {
        data.set(PARAMETER_MODULE, module);
    }

    public String getDescription() {
        return getParameterConverter().toString(data, PARAMETER_DESCRIPTION);
    }

    public void setDescription(String description) {
        data.set(PARAMETER_DESCRIPTION, description);
    }

    public String getFilterBuilderClass() {
        return getParameterConverter().toString(data, PARAMETER_FILTER_BUILDER_CLASS);
    }

    public void setFilterBuilderClass(String filterBuilderClass) {
        data.set(PARAMETER_FILTER_BUILDER_CLASS, filterBuilderClass);
    }
    
    public String getEntityClass() {
    	return getParameterConverter().toString(data, PARAMETER_ENTITY_CLASS);
	}
    
    public void setEntityClass(String entityClass) {
    	data.set(PARAMETER_ENTITY_CLASS, entityClass);
	}
}
