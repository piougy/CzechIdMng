package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
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
		@Index(name = "ux_idm_role_code", columnList = "code", unique = true),
		@Index(name = "idx_idm_role_name", columnList = "name"),
		@Index(name = "idx_idm_role_external_id", columnList = "external_id")})
public class IdmRole extends AbstractEntity implements Codeable, FormableEntity, Disableable, ExternalIdentifiable {
	
	private static final long serialVersionUID = -3099001738101202320L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;
	
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
	
	@Override
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
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
