package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Role
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role", indexes = { 
		@Index(name = "ux_idm_role_code", columnList = "code", unique = true),
		@Index(name = "idx_idm_role_name", columnList = "name"),
		@Index(name = "idx_idm_role_external_id", columnList = "external_id"),
		@Index(name = "idx_idm_role_environment", columnList = "environment"),
		@Index(name = "idx_idm_role_base_code", columnList = "base_code") })
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
	@Column(name = "base_code", length = DefaultFieldLengths.NAME, nullable = false)
	private String baseCode;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "environment", length = DefaultFieldLengths.NAME)
	private String environment;

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
	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	private RoleType roleType;

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
	
	@Formula("(select coalesce(count(1), 0) from idm_role_composition c where c.superior_id = id)")
	private long childrenCount;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_role_attr_def_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmFormDefinition identityRoleAttributeDefinition;

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

	/**
	 * @since 9.3.0
	 * @return
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * @since 9.3.0
	 * @param environment
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * @since 9.3.0
	 * @param baseCode
	 */
	public void setBaseCode(String baseCode) {
		this.baseCode = baseCode;
	}

	/**
	 * @since 9.3.0
	 * @return
	 */
	public String getBaseCode() {
		return baseCode;
	}

	/**
	 * @since 9.4.0
	 */
	public IdmFormDefinition getIdentityRoleAttributeDefinition() {
		return identityRoleAttributeDefinition;
	}

	/**
	 * @since 9.4.0
	 */
	public void setIdentityRoleAttributeDefinition(IdmFormDefinition identityRoleAttributeDefinition) {
		this.identityRoleAttributeDefinition = identityRoleAttributeDefinition;
	}

	/**
	 * Count of sub roles
	 * 
	 * @since 9.4.0
	 * @return
	 */
	public long getChildrenCount() {
		return childrenCount;
	}
	
	/**
	 * Count of sub roles
	 * 
	 * @param childrenCount
	 * @since 9.4.0
	 */
	public void setChildrenCount(long childrenCount) {
		this.childrenCount = childrenCount;
	}
}
