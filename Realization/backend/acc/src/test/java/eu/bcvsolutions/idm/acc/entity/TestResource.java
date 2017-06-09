package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDateTime;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Entity for test table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = TestResource.TABLE_NAME)
public class TestResource {
	
	public static final String TABLE_NAME = "test_resource";

	@Id
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "lastname", length = DefaultFieldLengths.NAME)
	private String lastname;
	@Column(name = "firstname", length = DefaultFieldLengths.NAME)
	private String firstname;
	@Column(name = "password", length = DefaultFieldLengths.NAME)
	private String password;
	@Column(name = "email", length = DefaultFieldLengths.NAME)
	private String email;
	@Column(name = "descrip", length = DefaultFieldLengths.NAME)
	private String descrip;
	@Column(name = "status", length = DefaultFieldLengths.NAME)
	private String status;
	@Column(name = "modified", length = DefaultFieldLengths.NAME)
	private LocalDateTime modified;
	@Column(name = "eav_attribute", length = DefaultFieldLengths.NAME)
	private String eavAttribute;

	public String getEavAttribute() {
		return eavAttribute;
	}

	public void setEavAttribute(String eavAttribute) {
		this.eavAttribute = eavAttribute;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescrip() {
		return descrip;
	}

	public void setDescrip(String descrip) {
		this.descrip = descrip;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getModified() {
		return modified;
	}

	public void setModified(LocalDateTime modified) {
		this.modified = modified;
	}
}
