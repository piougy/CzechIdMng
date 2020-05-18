package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Simple formable filter for find form instances.
 * 
 * @see FormableFilter
 * @see FormService#findFormInstances(eu.bcvsolutions.idm.core.api.domain.Identifiable, FormableFilter, eu.bcvsolutions.idm.core.security.api.domain.BasePermission...)
 * @author Radek Tomiška
 * @since 10.3.0
 */
public class DefaultFormableFilter extends DataFilter implements FormableFilter {

	public DefaultFormableFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public DefaultFormableFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public DefaultFormableFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(FormableDto.class, data, parameterConverter);
	}
	
}
