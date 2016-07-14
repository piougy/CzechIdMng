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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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
	
	@JsonManagedReference
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleAuthority> authorities;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "superiorRole", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleComposition> subRoles;
	
	@JsonProperty(access = Access.READ_ONLY)
	@OneToMany(mappedBy = "subRole")
	private List<IdmRoleComposition> superiorRoles;

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
		// workaround - orphan removal needs to preserve original list reference
		if (this.authorities == null) {
	        this.authorities = authorities;
	    } else {
	        this.authorities.clear();
	        this.authorities.addAll(authorities);
	    }
	}
	
	public List<IdmRoleComposition> getSubRoles() {
		if (subRoles == null) {
			subRoles = new ArrayList<>();
		}
		return subRoles;
	}
	
	public void setSubRoles(List<IdmRoleComposition> subRoles) {
		// workaround - orphan removal needs to preserve original list reference
		if (this.subRoles == null) {
	        this.subRoles = subRoles;
	    } else {
	        this.subRoles.clear();
	        this.subRoles.addAll(subRoles);
	    }
	}
	
	public List<IdmRoleComposition> getSuperiorRoles() {
		if (superiorRoles == null) {
			superiorRoles = new ArrayList<>();
		}
		return superiorRoles;
	}
	
	public void setSuperiorRoles(List<IdmRoleComposition> superiorRoles) {
		this.superiorRoles = superiorRoles;
	}
}
