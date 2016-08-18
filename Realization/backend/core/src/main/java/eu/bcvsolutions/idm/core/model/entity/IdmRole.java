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

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;

@Entity
@Table(name = "idm_role", indexes = { @Index(name = "ux_role_name", columnList = "name") })
public class IdmRole extends AbstractEntity implements IdentifiableByName {
	
	private static final long serialVersionUID = -3099001738101202320L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled = false;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role_type", nullable = false)
	private IdmRoleType roleType = IdmRoleType.TECHNICAL;

	@Audited
	@Column(name = "approve_add_workflow", length = DefaultFieldLengths.NAME)
	private String approveAddWorkflow;
	
	@Audited
	@Column(name = "approve_remove_workflow", length = DefaultFieldLengths.NAME)
	private String approveRemoveWorkflow;
	
	@Audited
	@Column(name = "description")
	private String description;
	
	@Audited
	@JsonManagedReference
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleAuthority> authorities;
	
	@Audited
	@JsonManagedReference
	@OneToMany(mappedBy = "superior", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleComposition> subRoles;
	
	@Audited
	@JsonProperty(access = Access.READ_ONLY)
	@OneToMany(mappedBy = "sub")
	private List<IdmRoleComposition> superiorRoles;

	@Override
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

	public String getApproveAddWorkflow() {
		return approveAddWorkflow;
	}

	public void setApproveAddWorkflow(String approveAddWorkflow) {
		this.approveAddWorkflow = approveAddWorkflow;
	}

	public String getApproveRemoveWorkflow() {
		return approveRemoveWorkflow;
	}

	public void setApproveRemoveWorkflow(String approveRemoveWorkflow) {
		this.approveRemoveWorkflow = approveRemoveWorkflow;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
