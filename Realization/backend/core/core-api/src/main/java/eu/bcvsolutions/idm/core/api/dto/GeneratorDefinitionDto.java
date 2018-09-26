package eu.bcvsolutions.idm.core.api.dto;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Generator value definition
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "generatorDefinitions")
public class GeneratorDefinitionDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String entityType;
	private IdmFormDefinitionDto formDefinition;
	@NotEmpty
	private String generatorType;

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}

	public String getGeneratorType() {
		return generatorType;
	}

	public void setGeneratorType(String generatorType) {
		this.generatorType = generatorType;
	}
}
