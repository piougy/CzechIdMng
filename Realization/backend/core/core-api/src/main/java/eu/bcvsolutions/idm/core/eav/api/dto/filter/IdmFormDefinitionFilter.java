package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Form definition filter.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormDefinitionFilter extends DataFilter {

	public static final String PARAMETER_TYPE = "type";
	public static final String PARAMETER_CODE = "code";
	public static final String PARAMETER_NAME = "name";
	public static final String PARAMETER_MAIN = "main";
	
	public IdmFormDefinitionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmFormDefinitionFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmFormDefinitionFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmFormDefinitionDto.class, data, parameterConverter);
	}	

	public String getType() {
		return getParameterConverter().toString(getData(), PARAMETER_TYPE);
	}

	public void setType(String type) {
		if (StringUtils.isEmpty(type)) {
    		data.remove(PARAMETER_TYPE);
    	} else {
    		data.put(PARAMETER_TYPE, Lists.newArrayList(type));
    	}
	}
	
	/**
	 * Types - OR.
	 * 
	 * @return filter by types (OR)
	 * @since 10.2.0
	 */
	public List<String> getTypes() {
		return getParameterConverter().toStrings(data, PARAMETER_TYPE);
	}
    
	/**
	 * Types - OR.
	 * 
	 * @return filter by types (OR)
	 * @since 10.2.0
	 */
    public void setTypes(List<String> types) {
    	if (CollectionUtils.isEmpty(types)) {
    		data.remove(PARAMETER_TYPE);
    	} else {
    		data.put(PARAMETER_TYPE, new ArrayList<Object>(types));
    	}
	}

	public String getCode() {
		return getParameterConverter().toString(getData(), PARAMETER_CODE);
	}

	public void setCode(String code) {
		set(PARAMETER_CODE, code);
	}

	public String getName() {
		return getParameterConverter().toString(getData(), PARAMETER_NAME);
	}

	public void setName(String name) {
		set(PARAMETER_NAME, name);
	}
	
	public void setMain(Boolean main) {
		set(PARAMETER_MAIN, main);
	}
	
	public Boolean getMain() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_MAIN);
	}
}
