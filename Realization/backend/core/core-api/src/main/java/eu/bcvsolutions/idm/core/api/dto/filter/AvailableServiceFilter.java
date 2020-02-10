package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Available Service filter
 *
 * @author Ondrej Husnik
 *
 *
 */
public class AvailableServiceFilter extends DataFilter {
    
    public AvailableServiceFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public AvailableServiceFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }

    public AvailableServiceFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(AvailableServiceDto.class , data, parameterConverter);
    }
}
