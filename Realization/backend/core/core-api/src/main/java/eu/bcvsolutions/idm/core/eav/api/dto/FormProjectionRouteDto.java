package eu.bcvsolutions.idm.core.eav.api.dto;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Form projection route - projection configuration.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "formProjectionRoutes")
public class FormProjectionRouteDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String ownerType;
	private IdmFormDefinitionDto formDefinition;
	
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public String getOwnerType() {
		return ownerType;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
}
