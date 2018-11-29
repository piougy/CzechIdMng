package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

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
	private UUID fromIdentityContract;

	@JsonProperty(access = Access.WRITE_ONLY)
	private UUID fromIdentity;

	@JsonProperty(access = Access.WRITE_ONLY)
	private List<UUID> identityRoles;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validFrom;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validTill;

	@JsonProperty(access = Access.WRITE_ONLY)
	private boolean useValidFromIdentity = true;

	@JsonProperty(access = Access.WRITE_ONLY)
	private boolean copyRoleParameters = true;

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

	public UUID getFromIdentityContract() {
		return fromIdentityContract;
	}

	public void setFromIdentityContract(UUID fromIdentityContract) {
		this.fromIdentityContract = fromIdentityContract;
	}

	public UUID getFromIdentity() {
		return fromIdentity;
	}

	public void setFromIdentity(UUID fromIdentity) {
		this.fromIdentity = fromIdentity;
	}

	public List<UUID> getIdentityRoles() {
		if (identityRoles == null) {
			return new ArrayList<UUID>();
		}
		return identityRoles;
	}

	public void setIdentityRoles(List<UUID> identityRoles) {
		this.identityRoles = identityRoles;
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

	public boolean isUseValidFromIdentity() {
		return useValidFromIdentity;
	}

	public void setUseValidFromIdentity(boolean useValidFromIdentity) {
		this.useValidFromIdentity = useValidFromIdentity;
	}

	public boolean isCopyRoleParameters() {
		return copyRoleParameters;
	}

	public void setCopyRoleParameters(boolean copyRoleParameters) {
		this.copyRoleParameters = copyRoleParameters;
	}

}
