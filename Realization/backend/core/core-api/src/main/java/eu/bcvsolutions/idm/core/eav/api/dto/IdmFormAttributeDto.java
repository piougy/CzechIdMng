package eu.bcvsolutions.idm.core.eav.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Niceable;
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
public class IdmFormAttributeDto extends AbstractDto implements UnmodifiableEntity, Niceable {

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
	private boolean unique;
	private BigDecimal max;
	private BigDecimal min;
	private String regex;
	private String validationMessage;
	
	public IdmFormAttributeDto() {
	}
	
	/**
	 * Creates simple short text attribute with same na as code
	 * 
	 * @param code
	 */
	public IdmFormAttributeDto(String code) {
		this(code, code);
	}
	
	/**
	 * Creates simple short text attribute 
	 * 
	 * @param code
	 * @param name
	 */
	public IdmFormAttributeDto(String code, String name) {
		this(code, name, PersistentType.SHORTTEXT);
	}
	
	/**
	 * Creates attribute with give persistent type and default face
	 * @param code
	 * @param name
	 * @param persistentType
	 */
	public IdmFormAttributeDto(String code, String name, PersistentType persistentType) {
		this(code, name, persistentType, null);
	}
	
	public IdmFormAttributeDto(String code, String name, PersistentType persistentType, String faceType) {
		Assert.notNull(code);
		Assert.notNull(persistentType);
		//
		this.code = code;
		this.name = name;
		this.persistentType = persistentType;
		this.faceType = faceType;
	}

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

	@Override
	public String getNiceLabel() {
		return getCode();
	}
	
	/**
	 * @return
	 * @since 9.4.0
	 */
	public boolean isUnique() {
		return unique;
	}

	/**
	 * @param unique
	 * @since 9.4.0
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * @return
	 * @since 9.4.0
	 */
	public BigDecimal getMax() {
		return max;
	}

	/**
	 * @param max
	 * @since 9.4.0
	 */
	public void setMax(BigDecimal max) {
		this.max = max;
	}

	/**
	 * @return
	 * @since 9.4.0
	 */
	public BigDecimal getMin() {
		return min;
	}

	/**
	 * @param min
	 * @since 9.4.0
	 */
	public void setMin(BigDecimal min) {
		this.min = min;
	}

	/**
	 * @return
	 * @since 9.4.0
	 */
	public String getRegex() {
		return regex;
	}

	/**
	 * @param regex
	 * @since 9.4.0
	 */
	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @return
	 * @since 9.4.0
	 */
	public String getValidationMessage() {
		return validationMessage;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @param validationMessage
	 * @since 9.4.0
	 */
	public void setValidationMessage(String validationMessage) {
		this.validationMessage = validationMessage;
	}
}
