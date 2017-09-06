package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Form attribute definition filter
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormAttributeFilter implements BaseFilter {

	private UUID formDefinitionId;
	private String definitionType;
	private String definitionName;
	private String code;

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public String getDefinitionType() {
		return definitionType;
	}

	public void setDefinitionType(String definitionType) {
		this.definitionType = definitionType;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public UUID getFormDefinitionId() {
		return formDefinitionId;
	}

	public void setFormDefinitionId(UUID formDefinitionId) {
		this.formDefinitionId = formDefinitionId;
	}

}
