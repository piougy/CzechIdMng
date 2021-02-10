package eu.bcvsolutions.idm.core.eav.api.dto;

import javax.validation.constraints.NotEmpty;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Form ttribute render - face type with configuration.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "formAttributeRenderers")
public class FormAttributeRendererDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	private PersistentType persistentType;
	private IdmFormDefinitionDto formDefinition;
	
	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}
	
	public PersistentType getPersistentType() {
		return persistentType;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
}
