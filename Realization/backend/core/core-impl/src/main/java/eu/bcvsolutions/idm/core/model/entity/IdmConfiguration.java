package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Application scope configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Audited
@XmlRootElement
@Table(name = "idm_configuration", indexes = { @Index(name = "ux_configuration_name", columnList = "name", unique = true) })
public class IdmConfiguration extends AbstractEntity implements Codeable {
	
	private static final long serialVersionUID = -8377477231407116537L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Column(name = "value")
	private String value;
	
	@NotNull
	@Column(name = "secured", nullable = false)
	private boolean secured;
	
	@NotNull
	@Column(name = "confidential", nullable = false)
	private boolean confidential;
	
	public IdmConfiguration() {
	}
	
	public IdmConfiguration(UUID id) {
		super(id);
	}
	
	public IdmConfiguration(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public IdmConfiguration(String name, String value, boolean secured, boolean confidential) {
		this(name, value);
		this.secured = secured;	
		this.confidential = confidential;	
	}

	/**
	 * Configuration property key
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Configuration property key
	 * 
	 * @return
	 */
	@Override
	@JsonIgnore
	public String getCode() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Configuration property value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Secured property is not readable without permission. Not secured configuration property is readable without authentication 
	 * 
	 * @return
	 */
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
	/**
	 * Secured negate alias
	 * @return
	 */
	public boolean isPublic() {
		return !secured;
	}

	public void setPublic(boolean notSecured) {
		this.secured = !notSecured;
	}
	
	/**
	 * Confidential property - wil be saved in confidential storage
	 * 
	 * @return
	 */
	public boolean isConfidential() {
		return confidential;
	}
	
	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
		if (confidential) {
			this.secured = true;
		}
	}
}
