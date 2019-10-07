package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Create concepts by identity and their roles. The dto is used only for create
 * new concepts for existing concepts.
 *
 * @author Ondrej Kopr
 *
 */
public class IdmRoleRequestByIdentityDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@JsonProperty(access = Access.WRITE_ONLY)
	private UUID roleRequest;

	@NotEmpty
	@JsonProperty(access = Access.WRITE_ONLY)
	private UUID identityContract;

	@JsonProperty(access = Access.WRITE_ONLY)
	private List<UUID> identityRoles;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validFrom;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validTill;

	@JsonProperty(access = Access.WRITE_ONLY)
	private boolean copyRoleParameters = false;

	public UUID getRoleRequest() {
		return roleRequest;
	}

	public void setRoleRequest(UUID roleRequest) {
		this.roleRequest = roleRequest;
	}

	public UUID getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public List<UUID> getIdentityRoles() {
		return identityRoles;
	}

	public void setIdentityRoles(List<UUID> identityRoles) {
		this.identityRoles = identityRoles;
	}

	public boolean isCopyRoleParameters() {
		return copyRoleParameters;
	}

	public void setCopyRoleParameters(boolean copyRoleParameters) {
		this.copyRoleParameters = copyRoleParameters;
	}

}
