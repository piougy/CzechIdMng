package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Form definition filter
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormDefinitionFilter extends DataFilter {

	private String type;
	private String code;
	private String name;
	private Boolean main;
	
	public IdmFormDefinitionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmFormDefinitionFilter(MultiValueMap<String, Object> data) {
		super(IdmFormDefinitionDto.class, data);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setMain(Boolean main) {
		this.main = main;
	}
	
	public Boolean getMain() {
		return main;
	}
}
