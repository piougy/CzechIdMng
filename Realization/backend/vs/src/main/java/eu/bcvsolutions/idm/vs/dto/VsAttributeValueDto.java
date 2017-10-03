package eu.bcvsolutions.idm.vs.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.vs.domain.VsValueChangeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for attribute value with marked type of change
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "accounts")
@ApiModel(description = "Attribute value with marked type of change")
public class VsAttributeValueDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private Object value;
	private Object oldValue;
	@ApiModelProperty(required = false, notes = "Type of value change")
	private VsValueChangeType change;

	public VsAttributeValueDto() {
	}
	
	public VsAttributeValueDto(Object value, Object oldValue, VsValueChangeType type) {
		this.value = value;
		this.oldValue = oldValue;
		this.change = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public VsValueChangeType getChange() {
		return change;
	}

	public void setChange(VsValueChangeType change) {
		this.change = change;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}
	
	

}
