package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;

/**
 * Filter for code lists
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class IdmCodeListFilter extends DataFilter implements ExternalIdentifiableFilter {

	public static final String PARAMETER_CODE = "code"; // PARAMETER_CODEABLE_IDENTIFIER can be used too

    public IdmCodeListFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmCodeListFilter(MultiValueMap<String, Object> data) {
        super(IdmCodeListDto.class, data);
    }
    
    public String getCode() {
		return (String) data.getFirst(PARAMETER_CODE);
	}

	public void setCode(String code) {
		data.set(PARAMETER_CODE, code);
	}
}
