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

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Role
 * 
 * TODO: add role's code
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role", indexes = { 
		@Index(name = "ux_idm_role_name", columnList = "name", unique = true),
		@Index(name = "idx_idm_role_external_id", columnList = "external_id")})
public class IdmRole extends AbstractEntity implements Codeable, FormableEntity, Disableable, ExternalIdentifiable {
	
	private static final long serialVersionUID = -3099001738101202320L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
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
	@Column(name = "approve_remove", nullable = false)
	private boolean approveRemove = false;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	@Deprecated // @since 8.2.0 - will be removed in 9 - business role redesign
	@OneToMany(mappedBy = "superior", cascade = CascadeType.ALL, orphanRemoval = true)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private List<IdmRoleComposition> subRoles;
	
	@Deprecated // @since 8.2.0 - will be removed in 9 - business role redesign
	@OneToMany(mappedBy = "sub")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private List<IdmRoleComposition> superiorRoles;

	@Deprecated // @since 8.2.0 - use solo endpoint
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private List<IdmRoleGuarantee> guarantees;

	@Deprecated // @since 8.2.0 - use solo endpoint
	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private List<IdmRoleCatalogueRole> roleCatalogues;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	@Audited
	@NotNull
	@Column(name = "can_be_requested", nullable = false)
	private boolean canBeRequested;

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

	public String getName() {
		return name;
	}
	
	@Override
	@JsonIgnore
	public String getCode() {
		return getName();
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

	public boolean isApproveRemove() {
		return approveRemove;
	}

	public void setApproveRemove(boolean approveRemove) {
		this.approveRemove = approveRemove;
	}

	public boolean isCanBeRequested() {
		return canBeRequested;
	}

	public void setCanBeRequested(boolean canBeRequested) {
		this.canBeRequested = canBeRequested;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
