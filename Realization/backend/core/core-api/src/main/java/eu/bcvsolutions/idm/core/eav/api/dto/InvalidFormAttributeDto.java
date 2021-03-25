package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * DTO for keep informations about invalidate attribute.
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "invalidFormAttributes")
public class InvalidFormAttributeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private Serializable ownerId; // UUID in most cases, but Identifiable is supported.
	private String ownerType;
	private String attributeCode; // form attribute code
	private String definitionCode; // form definiton code
	//
	// error states - filled, when validation not pass
	private boolean missingValue = false;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private String uniqueValue;
	private String regexValue;
	//
	private String message; // Custom message, when validation fails - localization key can be used.

	public InvalidFormAttributeDto() {
	}

	public InvalidFormAttributeDto(IdmFormAttributeDto formAttribute) {
		super(formAttribute);
		//
		this.attributeCode = formAttribute.getCode();
		this.message = formAttribute.getValidationMessage();
	}
	
	/**
	 * Returns {@code true} when all error states are empty => checked form value is valid.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return !missingValue
				&& minValue == null
				&& maxValue == null
				&& uniqueValue == null
				&& regexValue == null;
	}

	public String getAttributeCode() {
		return attributeCode;
	}

	public void setAttributeCode(String attributeCode) {
		this.attributeCode = attributeCode;
	}

	public boolean isMissingValue() {
		return missingValue;
	}

	public void setMissingValue(boolean missingValue) {
		this.missingValue = missingValue;
	}

	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	public String getUniqueValue() {
		return uniqueValue;
	}

	public void setUniqueValue(String uniqueValue) {
		this.uniqueValue = uniqueValue;
	}

	public String getRegexValue() {
		return regexValue;
	}

	public void setRegexValue(String regexValue) {
		this.regexValue = regexValue;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Owner identifier.
	 * 
	 * @return owner identifier
	 * @since 10.7.0
	 */
	public Serializable getOwnerId() {
		return ownerId;
	}
	
	/**
	 * Owner identifier.
	 * 
	 * @param ownerId owner identifier
	 * @since 10.7.0
	 */
	public void setOwnerId(Serializable ownerId) {
		this.ownerId = ownerId;
	}
	
	/**
	 * Owner type.
	 * 
	 * @see LookupService#getOwnerType(Class)
	 * @return owner type
	 * @since 10.7.0
	 */
	public String getOwnerType() {
		return ownerType;
	}
	
	/**
	 * Owner type.
	 * 
	 * @see LookupService#getOwnerType(Class)
	 * @param ownerType owner type
	 * @since 10.7.0
	 */
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	/**
	 * Textual information used in logs.
	 * 
	 * @since 10.7.0
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(String.format("Attribute code [%s]", attributeCode));
		if (missingValue) {
			result.append(" - missingValue [true]");
		}
		if (minValue != null) {
			result.append(String.format(" - minValue [%s]", minValue));
		}
		if (maxValue != null) {
			result.append(String.format(" - maxValue [%s]", maxValue));		
		}
		if (uniqueValue != null) {
			result.append(String.format(" - uniqueValue [%s]", uniqueValue));
		}
		if (regexValue != null) {
			result.append(String.format(" - regexValue [%s]", regexValue));
		}
		//
		return result.toString();
	}
	
	/**
	 * Related form definition code - more definitions can be validated.
	 * 
	 * @return form definition code
	 * @since 11.0.0
	 */
	public String getDefinitionCode() {
		return definitionCode;
	}
	
	/**
	 * Related form definition code - more definitions can be validated.
	 * 
	 * @param definitionCode form definition code
	 * @since 11.0.0
	 */
	public void setDefinitionCode(String definitionCode) {
		this.definitionCode = definitionCode;
	}
}
