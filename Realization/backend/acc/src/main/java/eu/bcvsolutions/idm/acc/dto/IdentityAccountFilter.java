package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityAccountFilter implements BaseFilter {

	private Long accountId;
	private Long identityId;
	private Long roleId;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getIdentityId() {
		return identityId;
	}

	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
}
