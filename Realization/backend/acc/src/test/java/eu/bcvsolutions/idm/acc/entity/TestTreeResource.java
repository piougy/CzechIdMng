package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDateTime;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Entity for tree test table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "test_tree_resource")
public class TestTreeResource {

	@Id
	@Column(name = "id", length = DefaultFieldLengths.NAME)
	private String id;
	@Column(name = "code", length = DefaultFieldLengths.NAME)
	private String code;
	@Column(name = "parent", length = DefaultFieldLengths.NAME)
	private String parent;
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "email", length = DefaultFieldLengths.NAME)
	private String email;
	@Column(name = "descrip", length = DefaultFieldLengths.NAME)
	private String descrip;
	@Column(name = "status", length = DefaultFieldLengths.NAME)
	private String status;
	@Column(name = "modified", length = DefaultFieldLengths.NAME)
	private LocalDateTime modified;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
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
