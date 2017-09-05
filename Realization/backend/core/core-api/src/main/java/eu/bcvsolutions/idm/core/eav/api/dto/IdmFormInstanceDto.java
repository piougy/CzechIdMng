package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Used as value holder for form service - form definition + their values by owner
 *
 * @author Radek Tomiška
 */
public class IdmFormInstanceDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@NotNull
	private IdmFormDefinitionDto formDefinition;
	@NotNull
	private Serializable ownerId;
	@NotEmpty
	private Class<? extends FormableEntity> ownerType;
	//
	private List<IdmFormValueDto> values;
	
	public IdmFormInstanceDto() {
	}
	
	public IdmFormInstanceDto(FormableEntity owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values) {
		Assert.notNull(owner);
		Assert.notNull(formDefinition);
		//
		ownerId = owner.getId();
		ownerType = owner.getClass();
		// todo: embedded?
		this.formDefinition = formDefinition;
		this.values = values;
	}

	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}

	public Serializable getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Serializable ownerId) {
		this.ownerId = ownerId;
	}

	public Class<? extends Identifiable> getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(Class<? extends FormableEntity> ownerType) {
		this.ownerType = ownerType;
	}

	public List<IdmFormValueDto> getValues() {
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<IdmFormValueDto> values) {
		this.values = values;
	}
	
	/**
	 * Returns form values as map, key is attribute code
	 * 
	 * @param values
	 * @return
	 */
	public Map<String, List<IdmFormValueDto>> toValueMap() {
		Assert.notNull(values);
		//
		Map<String, List<IdmFormValueDto>> results = new HashMap<>();
		for(IdmFormValueDto value : values) {
			IdmFormAttributeDto attribute = formDefinition.getMappedAttribute(value.getFormAttribute());
			String key = attribute.getCode();
			if (!results.containsKey(key)) {
				results.put(key, new ArrayList<>());
			}
			results.get(key).add(value);
		}
		
		return results;
	}
	
	/**
	 * Returns raw FormValue values as map, where key is attribute code
	 * 
	 * @param values
	 * @return
	 */
	public Map<String, List<Serializable>> toPersistentValueMap() {
		Assert.notNull(values);
		//
		Map<String, List<Serializable>> results = new HashMap<>();
		for(IdmFormValueDto value : values) {
			IdmFormAttributeDto attribute = formDefinition.getMappedAttribute(value.getFormAttribute());
			String key = attribute.getCode();
			if (!results.containsKey(key)) {
				results.put(key, new ArrayList<>());
			}
			results.get(key).add(value.getValue());
		}
		
		return results;
	}
	
	/**
	 * Returns raw values - usable for multi attribute values
	 * 
	 * @param attributeCode
	 * @return
	 */
	public List<Serializable> toPersistentValues(String attributeCode) {
		List<Serializable> values = toPersistentValueMap().get(attributeCode);
		if (ObjectUtils.isEmpty(values)) {
			return new ArrayList<>();
		}
		//
		return values;
	}
	
	/**
	 * Returns single FormValue by persistent type - usable for single attribute value
	 * 
	 * @see {@link PersistentType}
	 * @param attributeCode
	 * @return
	 * @throws IllegalArgumentException if attributte has multi values
	 */
	public Serializable toSinglePersistentValue(String attributeCode) {
		List<Serializable> values = toPersistentValues(attributeCode);
		if (values == null || values.isEmpty()) {
			return null;
		}
		if (values.size() > 1) {
			throw new IllegalArgumentException(MessageFormat.format("Attribute [{}] has mutliple values [{}]", attributeCode, values.size()));
		}
		return values.get(0);
	}
}
