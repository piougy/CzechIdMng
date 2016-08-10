package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;

/**
 * Application scope configuration
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Entity
@Table(name = "idm_configuration", indexes = { @Index(name = "ux_configuration_name", columnList = "name") })
public class IdmConfiguration extends AbstractEntity {
	
	private static final long serialVersionUID = -8377477231407116537L;

	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "secured")
	private boolean secured;
	
	public IdmConfiguration() {
	}
	
	public IdmConfiguration(Long id) {
		super(id);
	}
	
	public IdmConfiguration(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
}
