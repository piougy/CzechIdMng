package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;

/**
 * Filter for search in scripts. Attributes:
 * * text(from quick filter) - name
 * * category
 * * description
 * * usedIn - in which scripts is script used
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public class IdmScriptFilter extends DataFilter {

	public static final String PARAMETER_DESCRIPTION = "description";
	public static final String PARAMETER_CODE = "code";
	public static final String PARAMETER_USED_IN = "usedIn";
	public static final String PARAMETER_CATEGORY = "category";
	public static final String PARAMETER_IN_CATEGORY = "inCategory";

	
    public IdmScriptFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmScriptFilter(MultiValueMap<String, Object> data) {
        super(IdmScriptDto.class, data);
    }

    public List<IdmScriptCategory> getInCategory() {
		return getParameterConverter().toEnums(getData(), PARAMETER_IN_CATEGORY, IdmScriptCategory.class);
	}

	public void setInCategory(List<IdmScriptCategory> inCategory) {
		put(PARAMETER_IN_CATEGORY, inCategory);
	}

	public String getUsedIn() {
		return getParameterConverter().toString(getData(), PARAMETER_USED_IN);
	}

	public void setUsedIn(String usedIn) {
		set(PARAMETER_USED_IN, usedIn);
	}

	public String getDescription() {
		return getParameterConverter().toString(getData(), PARAMETER_DESCRIPTION);
    }

    public void setDescription(String description) {
    	set(PARAMETER_DESCRIPTION, description);
    }

    public IdmScriptCategory getCategory() {
    	return getParameterConverter().toEnum(getData(), PARAMETER_CATEGORY, IdmScriptCategory.class);
    }

    public void setCategory(IdmScriptCategory category) {
    	set(PARAMETER_CATEGORY, category);
    }

	public String getCode() {
		return getParameterConverter().toString(getData(), PARAMETER_CODE);
	}

	public void setCode(String code) {
		set(PARAMETER_CODE, code);
	}
}
