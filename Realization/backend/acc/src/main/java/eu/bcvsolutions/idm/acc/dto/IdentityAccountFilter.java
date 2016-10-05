package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityAccountFilter implements BaseFilter {

	private Long accountId;
	private IdmIdentity identity;
	private Long roleId;
	private Long systemId;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}
	
	public IdmIdentity getIdentity() {
		return identity;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
	
	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}
	
	public Long getSystemId() {
		return systemId;
	}
}
