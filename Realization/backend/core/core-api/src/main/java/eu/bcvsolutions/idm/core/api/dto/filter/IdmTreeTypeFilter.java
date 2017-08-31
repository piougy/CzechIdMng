package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;

/**
 * Filter for tree type
 *
 * @author Radek Tomiška
 */
public class IdmTreeTypeFilter extends DataFilter {

	public static final String PARAMETER_CODE = "code"; // PARAMETER_CODEABLE_IDENTIFIER can be used too

    public IdmTreeTypeFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmTreeTypeFilter(MultiValueMap<String, Object> data) {
        super(IdmTreeTypeDto.class, data);
    }
    
    public String getCode() {
		return (String) data.getFirst(PARAMETER_CODE);
	}

	public void setCode(String username) {
		data.set(PARAMETER_CODE, username);
	}
}
