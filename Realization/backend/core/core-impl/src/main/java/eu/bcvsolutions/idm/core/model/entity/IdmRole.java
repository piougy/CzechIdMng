package eu.bcvsolutions.idm.core.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.domain.RoleType;

/**
 * Role
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role", indexes = { 
		@Index(name = "ux_idm_role_name", columnList = "name", unique = true)})
public class IdmRole extends AbstractEntity implements IdentifiableByName, FormableEntity, Disableable {
	
	private static final long serialVersionUID = -3099001738101202320L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role_type", nullable = false)
	private RoleType roleType = RoleType.TECHNICAL;
	
	@Audited
	@NotNull
	@Column(name = "priority", nullable = false)
	private int priority = 0;

	@Audited
	@Column(name = "approve_add_workflow", length = DefaultFieldLengths.NAME)
	private String approveAddWorkflow;
	
	@Audited
	@Column(name = "approve_remove_workflow", length = DefaultFieldLengths.NAME)
	private String approveRemoveWorkflow;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleAuthority> authorities;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "superior", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleComposition> subRoles;
	
	@JsonProperty(access = Access.READ_ONLY)
	@OneToMany(mappedBy = "sub")
	private List<IdmRoleComposition> superiorRoles;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleGuarantee> guarantees;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdmRoleCatalogueRole> roleCatalogues;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	public IdmRole() {
	}
	
	public IdmRole(UUID id) {
		super(id);
	}

	public List<IdmRoleCatalogueRole> getRoleCatalogues() {
		if (roleCatalogues == null) {
			roleCatalogues = new ArrayList<>();
		}
		return roleCatalogues;
	}

	public void setRoleCatalogues(List<IdmRoleCatalogueRole> roleCatalogues) {
		if (this.roleCatalogues == null) {
	        this.roleCatalogues = roleCatalogues;
	    } else {
	        this.roleCatalogues.clear();
	        if(roleCatalogues != null){
	        	this.roleCatalogues.addAll(roleCatalogues);
	        }
	    }
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}
	
	public RoleType getRoleType() {
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
	        if(authorities != null){
	        	this.authorities.addAll(authorities);
	        }
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
	        if(subRoles != null){
	        	this.subRoles.addAll(subRoles);
	        }
	    }
	}
	
	public List<IdmRoleGuarantee> getGuarantees() {
		if (guarantees == null) {
			guarantees = new ArrayList<>();
		}
		return guarantees;
	}

	public void setGuarantees(List<IdmRoleGuarantee> guarantees) {
		// workaround - orphan removal needs to preserve original list reference
		if (this.guarantees == null) {
	        this.guarantees = guarantees;
	    } else {
	        this.guarantees.clear();
	        if(guarantees != null){
	        	this.guarantees.addAll(guarantees);
	        }
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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
