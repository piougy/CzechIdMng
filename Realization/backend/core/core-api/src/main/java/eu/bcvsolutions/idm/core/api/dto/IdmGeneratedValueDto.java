package eu.bcvsolutions.idm.core.api.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import io.swagger.annotations.ApiModel;

/**
 * Base DTO for generated values
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "generatedValues")
@ApiModel(description = "Definition for generator")
public class IdmGeneratedValueDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Size(max = DefaultFieldLengths.NAME)
	private String entityType;
	@Size(max = DefaultFieldLengths.NAME)
	private String generatorType;
	@NotNull
	@Max(99999)
	private short seq = 0;
	private ConfigurationMap generatorProperties;
	@NotNull
	private boolean disabled = false;
	@NotNull
	private boolean regenerateValue = true;


	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getGeneratorType() {
		return generatorType;
	}

	public void setGeneratorType(String generatorType) {
		this.generatorType = generatorType;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public void setGeneratorProperties(ConfigurationMap generatorProperties) {
		this.generatorProperties = generatorProperties;
	}

	public ConfigurationMap getGeneratorProperties() {
		return generatorProperties;
	}

	public boolean isRegenerateValue() {
		return regenerateValue;
	}

	public void setRegenerateValue(boolean regenerateValue) {
		this.regenerateValue = regenerateValue;
	}
}
