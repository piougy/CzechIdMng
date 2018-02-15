package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
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

    private String description;
    private IdmScriptCategory category;    
    private String code;
    private String usedIn;
    private List<IdmScriptCategory> inCategory; // or
    
    public IdmScriptFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmScriptFilter(MultiValueMap<String, Object> data) {
        super(IdmScriptDto.class, data);
    }

    public List<IdmScriptCategory> getInCategory() {
    	if (inCategory == null) {
    		inCategory = new ArrayList<>();
    	}
		return inCategory;
	}

	public void setInCategory(List<IdmScriptCategory> inCategory) {
		this.inCategory = inCategory;
	}

	public String getUsedIn() {
		return usedIn;
	}

	public void setUsedIn(String usedIn) {
		this.usedIn = usedIn;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IdmScriptCategory getCategory() {
        return category;
    }

    public void setCategory(IdmScriptCategory category) {
        this.category = category;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
