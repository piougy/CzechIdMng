package eu.bcvsolutions.idm.example.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;

/**
 * Filter for example products
 * 
 * @author Radek Tomiška
 *
 */
public class ExampleProductFilter extends DataFilter implements DisableableFilter {
	
	/**
	 * Product name
	 */
	public static final String PARAMETER_NAME = "name";
	
	public ExampleProductFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public ExampleProductFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public ExampleProductFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(ExampleProductDto.class, data, parameterConverter);
	}
	
	public String getName() {
		return getParameterConverter().toString(getData(), PARAMETER_NAME);
	}

	public void setName(String username) {
		set(PARAMETER_NAME, username);
	}
}
