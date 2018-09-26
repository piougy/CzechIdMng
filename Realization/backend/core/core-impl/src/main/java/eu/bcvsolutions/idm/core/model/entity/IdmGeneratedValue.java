package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

@Entity
@Table(name = "idm_generated_value", indexes = {})
public class IdmGeneratedValue extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@Column(name = "description")
	private String description;

	@Audited
	@NotNull
	@Column(name = "entity_type", nullable = false)
	private String entityType;

	@Audited
	@NotNull
	@Column(name = "generator_type", nullable = false)
	private String generatorType;

	@Audited
	@NotNull
	@Column(name = "seq", nullable = false)
	private short seq = 0;

	@Column(name = "generator_properties", length = Integer.MAX_VALUE)
	private ConfigurationMap generatorProperties;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled = false;

	
	@Audited
	@NotNull
	@Column(name = "regenerate_value", nullable = false)
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
