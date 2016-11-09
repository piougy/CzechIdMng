package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Application scope configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@XmlRootElement
@Table(name = "idm_configuration", indexes = { @Index(name = "ux_configuration_name", columnList = "name", unique = true) })
public class IdmConfiguration extends AbstractEntity implements IdentifiableByName {
	
	private static final long serialVersionUID = -8377477231407116537L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "secured")
	private boolean secured;
	
	public IdmConfiguration() {
	}
	
	public IdmConfiguration(UUID id) {
		super(id);
	}
	
	public IdmConfiguration(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
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
