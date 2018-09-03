package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

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
	@Column(name = "ID", length = DefaultFieldLengths.NAME)
	private String id;
	@Column(name = "CODE", length = DefaultFieldLengths.NAME)
	private String code;
	@Column(name = "PARENT", length = DefaultFieldLengths.NAME)
	private String parent;
	@Column(name = "NAME", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "EMAIL", length = DefaultFieldLengths.NAME)
	private String email;
	@Column(name = "DESCRIPT", length = DefaultFieldLengths.NAME)
	private String descrip;
	@Column(name = "STATUS", length = DefaultFieldLengths.NAME)
	private String status;
	@Column(name = "MODIFIED", length = DefaultFieldLengths.NAME)
	private DateTime modified;

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

	public DateTime getModified() {
		return modified;
	}

	public void setModified(DateTime modified) {
		this.modified = modified;
	}

}
