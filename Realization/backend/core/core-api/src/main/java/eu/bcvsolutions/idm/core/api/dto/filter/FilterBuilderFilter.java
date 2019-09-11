package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Filter builder filter
 *
 * @author Kolychev Artem
 */
public class FilterBuilderFilter extends DataFilter {

    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_TEXT = "text";
    public static final String PARAMETER_MODULE = "module";
    public static final String PARAMETER_DESCRIPTION = "description";
    public static final String PARAMETER_DISABLED = "disabled";
    public static final String PARAMETER_FILTER_BUILDER_CLASS = "filterBuilderClass";
    public static final String PARAMETER_ENTITY_CLASS = "entityClass";
    public static final String PARAMETER_ENTITY_TYPE = "entityType";

    public FilterBuilderFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public FilterBuilderFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }

    public FilterBuilderFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(null, data, parameterConverter);
    }

    public String getName() {
        return getParameterConverter().toString(data, PARAMETER_NAME);
    }

    public void setName(String name) {
        data.set(PARAMETER_NAME, name);
    }

    @Override
    public String getText() {
        return getParameterConverter().toString(data, PARAMETER_TEXT);
    }

    @Override
    public void setText(String text) {
        data.set(PARAMETER_TEXT, text);
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

    public Boolean getDisabled() {
        return getParameterConverter().toBoolean(data, PARAMETER_DISABLED);
    }

    public void setDisabled(Boolean disabled) {
        data.set(PARAMETER_DISABLED, disabled);
    }

    public String getFilterBuilderClass() {
        return getParameterConverter().toString(data, PARAMETER_FILTER_BUILDER_CLASS);
    }

    public void setFilterBuilderClass(String filterBuilderClass) {
        data.set(PARAMETER_FILTER_BUILDER_CLASS, filterBuilderClass);
    }

    public void setEntityType(String entityClass) {
        data.set(PARAMETER_ENTITY_TYPE, entityClass);
    }

    public String getEntityType() {
        return getParameterConverter().toString(data, PARAMETER_ENTITY_TYPE);
    }

}
