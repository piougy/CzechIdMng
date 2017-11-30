package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDateTime;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Entity for test (role) table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = TestRoleResource.TABLE_NAME)
public class TestRoleResource {
	
	public static final String TABLE_NAME = "test_role_resource";

	@Id
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "type", length = DefaultFieldLengths.NAME)
	private String type;
	@Column(name = "priority")
	private int priority;
	@Column(name = "description", length = DefaultFieldLengths.NAME)
	private String description;
	@Column(name = "approve_remove", length = DefaultFieldLengths.NAME)
	private Boolean approveRemove = Boolean.FALSE;
	@Column(name = "modified", length = DefaultFieldLengths.NAME)
	private LocalDateTime modified;
	@Column(name = "status", length = DefaultFieldLengths.NAME)
	private String status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getApproveRemove() {
		return approveRemove;
	}

	public void setApproveRemove(Boolean approveRemove) {
		this.approveRemove = approveRemove;
	}

	public LocalDateTime getModified() {
		return modified;
	}

	public void setModified(LocalDateTime modified) {
		this.modified = modified;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
}
