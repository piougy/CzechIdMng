package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for attribute in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "attributes")
@ApiModel(description = "Attribute of request item")
public class IdmRequestItemAttributeDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Name of attribute")
	private String name;
	private boolean multivalue;
	private boolean changed = false;
	private IdmRequestAttributeValueDto value;
	private List<IdmRequestAttributeValueDto> values;

	public IdmRequestItemAttributeDto() {
	}
	
	public IdmRequestItemAttributeDto(String name, boolean multiValue, boolean changed) {
		this.name = name;
		this.multivalue = multiValue;
		this.changed = changed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public IdmRequestAttributeValueDto getValue() {
		return value;
	}

	public void setValue(IdmRequestAttributeValueDto value) {
		this.value = value;
	}

	public List<IdmRequestAttributeValueDto> getValues() {
		if(values == null){
			this.values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<IdmRequestAttributeValueDto> values) {
		this.values = values;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
