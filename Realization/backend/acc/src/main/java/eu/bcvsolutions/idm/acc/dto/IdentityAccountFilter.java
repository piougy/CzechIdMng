package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityAccountFilter implements BaseFilter {

	private UUID accountId;
	private UUID identityId;
	private UUID roleId;
	private UUID systemId;
	private UUID identityRoleId;
	private Boolean ownership;

	public Boolean isOwnership() {
		return ownership;
	}

	public void setOwnership(Boolean ownership) {
		this.ownership = ownership;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}
	
	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
	
	public UUID getSystemId() {
		return systemId;
	}

	public UUID getIdentityRoleId() {
		return identityRoleId;
	}

	public void setIdentityRoleId(UUID identityRoleId) {
		this.identityRoleId = identityRoleId;
	}
}
