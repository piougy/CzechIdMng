package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter
 * 
 * @author Radek Tomiška
 *
 * @param <O> value owner
 */
public class IdmFormValueFilter<O extends FormableEntity> extends DataFilter {

	private UUID definitionId;
	private UUID attributeId;
	private O owner;
	private PersistentType persistentType;

	public IdmFormValueFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmFormValueFilter(MultiValueMap<String, Object> data) {
		super(IdmFormValueDto.class, data);
	}

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

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

}
