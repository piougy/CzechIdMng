package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.model.domain.BasePermission;
import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.domain.GroupPermission;

/**
 * Role privileges
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Entity
@Table(name = "idm_role_authority")
public class IdmRoleAuthority extends AbstractEntity {
	
	private static final long serialVersionUID = -4935521717718151720L;
	public static final String TARGET_ACTION_SEPARATOR = "_";

	@NotNull
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private IdmRole role;
	/**
	 * Group
	 */
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "target_permission", length = DefaultFieldLengths.NAME, nullable = false)
	private String target;
	/**
	 * Base permission
	 */
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "action_permission", length = DefaultFieldLengths.NAME, nullable = false)
	private String action;

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public void setTargetPermission(GroupPermission group) {
		this.target = group == null ? null : group.getName();
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public void setActionPermission(BasePermission permission) {
		this.action = permission == null ? null : permission.getName();
	}
	
	public String getAuthority() {
		return target + TARGET_ACTION_SEPARATOR + action;
	}
}
