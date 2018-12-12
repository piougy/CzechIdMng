package eu.bcvsolutions.idm.core.eav.api.dto;

import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * DTO for keep informations about invalidate attribute
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "invalidFormAttributes")
public class InvalidFormAttributeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private String attributeCode;
	private boolean missingValue = false;

	public InvalidFormAttributeDto() {
		super();
	}

	public InvalidFormAttributeDto(IdmFormAttributeDto formAttribute) {
		Assert.notNull(formAttribute);
		this.attributeCode = formAttribute.getCode();
		this.setId(formAttribute.getId());
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
}
