package eu.bcvsolutions.idm.core.eav.api.dto;

import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Form attribute dto
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "formAttributes")
public class IdmFormAttributeDto extends AbstractDto implements UnmodifiableEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmFormDefinitionDto.class)
	private UUID formDefinition;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String code;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String name;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Size(max = DefaultFieldLengths.NAME)
	private String placeholder;
	@NotNull
	private PersistentType persistentType;
	private String faceType;
	private boolean multiple;
	private boolean required;
	private boolean readonly;
	private boolean confidential;
	@Max(99999)
	private Short seq;
	private String defaultValue;
	private boolean unmodifiable = false;

	public UUID getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(UUID formDefinition) {
		this.formDefinition = formDefinition;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public Short getSeq() {
		return seq;
	}

	public void setSeq(Short seq) {
		this.seq = seq;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean isUnmodifiable() {
		return unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
	
	public void setFaceType(String faceType) {
		this.faceType = faceType;
	}
	
	public String getFaceType() {
		return faceType;
	}
}
