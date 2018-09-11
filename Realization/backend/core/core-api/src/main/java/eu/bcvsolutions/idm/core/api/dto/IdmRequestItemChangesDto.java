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
 * DTO for request item with marked changed values
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "items")
@ApiModel(description = "Request item with marked changed values")
public class IdmRequestItemChangesDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Request item")
	private IdmRequestItemDto requestItem;
	@ApiModelProperty(required = false, notes = "Object attributes with mark changes")
	private List<IdmRequestItemAttributeDto> attributes;

	public IdmRequestItemDto getRequestItem() {
		return requestItem;
	}

	public void setRequestItem(IdmRequestItemDto requestItem) {
		this.requestItem = requestItem;
	}

	public List<IdmRequestItemAttributeDto> getAttributes() {
		if(attributes == null) {
			attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<IdmRequestItemAttributeDto> attributes) {
		this.attributes = attributes;
	}
}
