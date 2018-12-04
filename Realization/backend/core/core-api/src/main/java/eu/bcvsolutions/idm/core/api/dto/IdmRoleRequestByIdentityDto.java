package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.collect.Lists;

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
	private List<UUID> roles;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validFrom;

	@JsonProperty(access = Access.WRITE_ONLY)
	private LocalDate validTill;

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

	public List<UUID> getRoles() {
		if (roles == null) {
			return Lists.newArrayList();
		}
		return roles;
	}

	public void setRoles(List<UUID> roles) {
		this.roles = roles;
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
}
