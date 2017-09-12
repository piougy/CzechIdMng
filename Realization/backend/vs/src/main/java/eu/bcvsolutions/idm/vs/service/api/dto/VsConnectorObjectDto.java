package eu.bcvsolutions.idm.vs.service.api.dto;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for account object in virtual system with marked changes (against specific VS request)
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "objects")
@ApiModel(description = "Account object in virtual system with marked changes (against specific VS request)")
public class VsConnectorObjectDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique account identifier. UID on system and for connector.")
	private String uid;
	@ApiModelProperty(required = false, notes = "Object attributes with mark changes")
	private List<VsAttributeDto> attributes;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<VsAttributeDto> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<VsAttributeDto> attributes) {
		this.attributes = attributes;
	}
}
