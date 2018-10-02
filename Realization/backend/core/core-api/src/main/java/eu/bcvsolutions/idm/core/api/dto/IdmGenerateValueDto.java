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
@Relation(collectionRelation = "generateValues")
@ApiModel(description = "Definition for configured generator")
public class IdmGenerateValueDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Size(max = DefaultFieldLengths.NAME)
	private String dtoType;
	@Size(max = DefaultFieldLengths.NAME)
	private String generatorType;
	@NotNull
	@Max(99999)
	private short seq = 11;
	private ConfigurationMap generatorProperties;
	@NotNull
	private boolean disabled = false;
	@NotNull
	private boolean regenerateValue = false;


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

	public String getDtoType() {
		return dtoType;
	}

	public void setDtoType(String dtoType) {
		this.dtoType = dtoType;
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
