package eu.bcvsolutions.idm.core.model.entity.thin;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Immutable;
import javax.validation.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Role - thin variant
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Entity
@Immutable
@Table(name = "idm_role")
public class IdmRoleThin extends AbstractEntity implements Codeable, FormableEntity, Disableable, ExternalIdentifiable {

	private static final long serialVersionUID = -3099001738101202320L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "base_code", length = DefaultFieldLengths.NAME, nullable = false)
	private String baseCode;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "environment", length = DefaultFieldLengths.NAME)
	private String environment;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;

	@NotNull
	@Column(name = "priority", nullable = false)
	private int priority = 0;

	@Column(name = "approve_remove", nullable = false)
	private boolean approveRemove = false;

	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	@NotNull
	@Column(name = "can_be_requested", nullable = false)
	private boolean canBeRequested;

	@Column(name = "identity_role_attr_def_id")
	private UUID identityRoleAttributeDefinition;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getBaseCode() {
		return baseCode;
	}

	public void setBaseCode(String baseCode) {
		this.baseCode = baseCode;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isApproveRemove() {
		return approveRemove;
	}

	public void setApproveRemove(boolean approveRemove) {
		this.approveRemove = approveRemove;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isCanBeRequested() {
		return canBeRequested;
	}

	public void setCanBeRequested(boolean canBeRequested) {
		this.canBeRequested = canBeRequested;
	}

	public UUID getIdentityRoleAttributeDefinition() {
		return identityRoleAttributeDefinition;
	}

	public void setIdentityRoleAttributeDefinition(UUID identityRoleAttributeDefinition) {
		this.identityRoleAttributeDefinition = identityRoleAttributeDefinition;
	}
}
