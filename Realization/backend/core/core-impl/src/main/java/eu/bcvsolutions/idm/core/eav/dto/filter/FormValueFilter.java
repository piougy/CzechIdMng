package eu.bcvsolutions.idm.core.eav.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form values filter
 * 
 * @author Radek Tomiška
 *
 * @param <O>
 */
public class FormValueFilter<O extends FormableEntity> implements BaseFilter {

	private IdmFormDefinition formDefinition;
	private IdmFormAttribute formAttribute;
	private O owner;

	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormAttribute getFormAttribute() {
		return formAttribute;
	}
	
	public void setFormAttribute(IdmFormAttribute formAttribute) {
		this.formAttribute = formAttribute;
	}

	public O getOwner() {
		return owner;
	}

	public void setOwner(O owner) {
		this.owner = owner;
	}

}
