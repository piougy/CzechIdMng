package eu.bcvsolutions.idm.core.eav.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter
 * 
 * @author Radek Tomiška
 *
 * @param <O>
 */
public class FormValueFilter<O extends FormableEntity> implements BaseFilter {

	private UUID formDefinitionId;
	private UUID formAttributeId;
	private O owner; // TODO: owner id and type

	public UUID getFormDefinitionId() {
		return formDefinitionId;
	}

	public void setFormDefinitionId(UUID formDefinitionId) {
		this.formDefinitionId = formDefinitionId;
	}

	public UUID getFormAttributeId() {
		return formAttributeId;
	}

	public void setFormAttributeId(UUID formAttributeId) {
		this.formAttributeId = formAttributeId;
	}

	public O getOwner() {
		return owner;
	}

	public void setOwner(O owner) {
		this.owner = owner;
	}

}
