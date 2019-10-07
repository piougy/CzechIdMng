package eu.bcvsolutions.idm.core.security.api.dto;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Evaluator dto - definition only
 * 
 * @author Radek Tomiška
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "authorizationEvaluators")
public class AuthorizationEvaluatorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String entityType;
	@NotEmpty
	private String evaluatorType;
	private boolean supportsPermissions;
	private IdmFormDefinitionDto formDefinition;
	
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getEvaluatorType() {
		return evaluatorType;
	}
	
	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	public void setSupportsPermissions(boolean supportsPermissions) {
		this.supportsPermissions = supportsPermissions;
	}
	
	public boolean isSupportsPermissions() {
		return supportsPermissions;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
}
