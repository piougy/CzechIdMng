package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;

/**
 * Filter for code list items
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class IdmCodeListItemFilter extends DataFilter implements ExternalIdentifiableFilter {

	public static final String PARAMETER_CODE = "code"; // PARAMETER_CODEABLE_IDENTIFIER can be used too
	public static final String PARAMETER_CODE_LIST_ID = "codeListId";

    public IdmCodeListItemFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmCodeListItemFilter(MultiValueMap<String, Object> data) {
        super(IdmCodeListItemDto.class, data);
    }
    
    public String getCode() {
		return (String) data.getFirst(PARAMETER_CODE);
	}

	public void setCode(String code) {
		data.set(PARAMETER_CODE, code);
	}
	
	public UUID getCodeListId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_CODE_LIST_ID));
	}
	
	public void setCodeListId(UUID codeListId) {
		data.set(PARAMETER_CODE_LIST_ID, codeListId);
	}
}
