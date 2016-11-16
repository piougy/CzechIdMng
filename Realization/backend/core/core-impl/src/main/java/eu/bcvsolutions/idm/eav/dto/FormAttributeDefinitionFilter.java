package eu.bcvsolutions.idm.eav.dto;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form attribute definition filter
 * 
 * @author Radek Tomiška
 *
 */
public class FormAttributeDefinitionFilter implements BaseFilter {

	private IdmFormDefinition formDefinition;
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

}
