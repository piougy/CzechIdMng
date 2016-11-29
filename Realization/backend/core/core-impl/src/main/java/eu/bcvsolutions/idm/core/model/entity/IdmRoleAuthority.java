package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;

/**
 * Role privileges
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_role_authority", indexes = {
		@Index(name = "idx_idm_role_authority_role", columnList = "role_id")
})
public class IdmRoleAuthority extends AbstractEntity {
	
	private static final long serialVersionUID = -4935521717718151720L;

	@Audited(withModifiedFlag=true)
	@NotNull
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private IdmRole role;
	/**
	 * Group
	 */
	@Audited(withModifiedFlag=true)
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "target_permission", length = DefaultFieldLengths.NAME, nullable = false)
	private String target;
	/**
	 * Base permission
	 */
	@Audited(withModifiedFlag=true)
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
		return new DefaultGrantedAuthority(target, action).getAuthority();
	}
}
