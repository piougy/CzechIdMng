package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter
 * 
 * @author Radek Tomiška
 *
 * @param <O> value owner
 */
public class IdmFormValueFilter<O extends FormableEntity> implements BaseFilter {

	private UUID definitionId;
	private UUID attributeId;
	private O owner;

	public UUID getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(UUID definitionId) {
		this.definitionId = definitionId;
	}

	public UUID getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(UUID attributeId) {
		this.attributeId = attributeId;
	}

	public O getOwner() {
		return owner;
	}

	public void setOwner(O owner) {
		this.owner = owner;
	}

}
