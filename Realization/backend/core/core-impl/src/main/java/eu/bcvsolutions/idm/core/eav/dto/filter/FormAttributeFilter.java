package eu.bcvsolutions.idm.core.eav.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form attribute definition filter
 * 
 * @author Radek Tomiška
 *
 */
public class FormAttributeFilter implements BaseFilter {

	private IdmFormDefinition formDefinition;
	private String definitionType;
	private String definitionName;
	private String name;

	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}
