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
	private IdmIdentity identity;
	private UUID roleId;
	private UUID systemId;

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}
	
	public IdmIdentity getIdentity() {
		return identity;
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
}
