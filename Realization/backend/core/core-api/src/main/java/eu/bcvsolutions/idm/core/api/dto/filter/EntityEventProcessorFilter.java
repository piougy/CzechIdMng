package eu.bcvsolutions.idm.core.api.dto.filter;

import java.io.Serializable;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Entity event processors filter.
 * 
 * @author Radek Tomiška
 */
public class EntityEventProcessorFilter extends DataFilter {

	public static final String PARAMETER_NAME = "name";  // equals
	public static final String PARAMETER_MODULE = "module"; // equals
	public static final String PARAMETER_DESCRIPTION  = "description"; // like	
	public static final String PARAMETER_EVENT_TYPES  = "eventTypes"; // and - processor has to support all
	public static final String PARAMETER_ENTITY_TYPE  = "entityType"; // equals
	public static final String PARAMETER_CONTENT_CLASS  = "contentClass"; // equals

	public EntityEventProcessorFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public EntityEventProcessorFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public EntityEventProcessorFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(EntityEventProcessorDto.class , data, parameterConverter);
    }
	
	@SuppressWarnings("unchecked")
	public Class<? extends Serializable> getContentClass() {
		return (Class<? extends Serializable>) getData().getFirst(PARAMETER_CONTENT_CLASS);
	}

	public void setContentClass(Class<? extends Serializable> contentClass) {
		set(PARAMETER_CONTENT_CLASS, contentClass);
	}
	
	public String getEntityType() {
		return getParameterConverter().toString(getData(), PARAMETER_ENTITY_TYPE);
	}

	public void setEntityType(String entityType) {
		set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public List<String> getEventTypes() {
		return getParameterConverter().toStrings(getData(), PARAMETER_EVENT_TYPES);
	}
	
	public void setEventTypes(List<String> eventTypes) {
		put(PARAMETER_EVENT_TYPES, eventTypes);
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
}
