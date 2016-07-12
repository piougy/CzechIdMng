package eu.bcvsolutions.idm.core.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;

@Entity
@Table(name = "idm_role", indexes = { @Index(name = "ux_role_name", columnList = "name") })
public class IdmRole extends AbstractEntity {
	
	private static final long serialVersionUID = -3099001738101202320L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled = false;
	
	@NotNull
	@Column(name = "approvable", nullable = false)
	private boolean approvable = false;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role_type", nullable = false)
	private IdmRoleType roleType = IdmRoleType.TECHNICAL;
	
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
	private List<IdmRoleAuthority> authorities;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isApprovable() {
		return approvable;
	}

	public void setApprovable(boolean approvable) {
		this.approvable = approvable;
	}
	
	public void setRoleType(IdmRoleType roleType) {
		this.roleType = roleType;
	}
	
	public IdmRoleType getRoleType() {
		return roleType;
	}
	
	public List<IdmRoleAuthority> getAuthorities() {
		if (authorities == null) {
			authorities = new ArrayList<>();
		}
		return authorities;
	}

	public void setAuthorities(List<IdmRoleAuthority> authorities) {
		this.authorities = authorities;
	}
}
