package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import io.swagger.annotations.ApiModelProperty;

/**
 * Used as value holder for form service - form definition + their values by owner.
 * 
 * TODO: create "SmartFormInstanceDto" - only string "codes", "owner" and "values" can be given, without definition and attributes uuid 
 *
 * @author Radek Tomiška
 */
public class IdmFormInstanceDto implements BaseDto {
	
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	private IdmFormDefinitionDto formDefinition;
	@JsonDeserialize(as = String.class)
	private Serializable ownerId;
	@NotNull
	private Class<? extends Identifiable> ownerType;
	private List<IdmFormValueDto> values;
	private List<InvalidFormAttributeDto> validationErrors;
	@JsonDeserialize(as = UUID.class)
	@ApiModelProperty(required = true, notes = "Unique uuid identifier. It's ID of FormDefinition here.", dataType = "java.util.UUID")
	private UUID id;
	
	public IdmFormInstanceDto() {
		super();
	}
	
	public IdmFormInstanceDto(UUID id) {
		this.id = id;
	}
	
	public IdmFormInstanceDto(Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values) {
		Assert.notNull(owner, "Owner is required for form instance.");
		Assert.notNull(formDefinition, "Form definition is required for form instance.");
		//
		this.ownerId = owner.getId();
		this.ownerType = owner.getClass();
		this.formDefinition = formDefinition;
		if (formDefinition != null) {
			this.id = formDefinition.getId();
		}
		this.values = values;
	}
	
	public IdmFormInstanceDto(Identifiable owner, IdmFormDefinitionDto formDefinition, IdmFormDto form) {
		this(owner, formDefinition, form == null ? null : form.getValues());
	}

	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		Assert.notNull(formDefinition, "Form definition is required for form instance.");
		
		this.id = formDefinition.getId();
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

	public void setOwnerType(Class<? extends Identifiable> ownerType) {
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
		Map<String, List<IdmFormValueDto>> results = new HashMap<>();
		for(IdmFormValueDto value : getValues()) {
			IdmFormAttributeDto attribute = formDefinition.getMappedAttribute(value.getFormAttribute());
			if (attribute == null) {
				throw new CoreException("Form attribute with code [" + value.getFormAttribute() + "] not found in definition [" + formDefinition.getId() + "]");
			}
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
		Map<String, List<Serializable>> results = new HashMap<>();
		//
		for(IdmFormValueDto value : getValues()) {
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
	
	/**
	 * Returns form values as map, key is attribute code
	 *
	 * @param values
	 * @return
	 */
	public MultiValueMap<String, Object> toMultiValueMap() {
		MultiValueMap<String, Object> results = new LinkedMultiValueMap<>();
		for(IdmFormValueDto value : getValues()) {
			IdmFormAttributeDto attribute = formDefinition.getMappedAttribute(value.getFormAttribute());
			String key = attribute.getCode();
			results.add(key, value.getValue());
		}		
		return results;
	}
	
	/**
	 * Returns attribute definition by identifier.
	 *
	 * @param formAttributeId
	 * @return
	 * @since 8.2.0
	 */
	public IdmFormAttributeDto getMappedAttribute(UUID formAttributeId) {
		return getFormDefinition().getMappedAttribute(formAttributeId);
	}
	
	/**
	 * Returns attribute definition by code.
	 *
	 * @param attributeCode
	 * @return
	 * @since 8.2.0
	 */
	public IdmFormAttributeDto getMappedAttributeByCode(String attributeCode) {
		return getFormDefinition().getMappedAttributeByCode(attributeCode);
	}
	
	/**
	 * Set evaluated validations.
	 * 
	 * @param validationErrors
	 * @since 9.4.0
	 */
	public void setValidationErrors(List<InvalidFormAttributeDto> validationErrors) {
		this.validationErrors = validationErrors;
	}
	
	/**
	 * Evaluated validations. Returns {@code null}, when validation was not checked.
	 * 
	 * @return
	 * @since 9.4.0
	 */
	public List<InvalidFormAttributeDto> getValidationErrors() {
		return validationErrors;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		try {
			this.id = EntityUtils.toUuid(id);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("FormDefinition supports only UUID identifier.", ex);
		}
	}
}
