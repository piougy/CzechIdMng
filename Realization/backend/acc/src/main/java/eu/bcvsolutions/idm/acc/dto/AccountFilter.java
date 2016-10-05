package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.core.model.dto.BaseFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class AccountFilter implements BaseFilter {
	
	private Long systemEntityId;
	
	private Long systemId;
	
	private Long identityId;
	
	private String uid;
	
	private AccountType accountType;
	
	public Long getSystemEntityId() {
		return systemEntityId;
	}
	
	public void setSystemEntityId(Long systemEntityId) {
		this.systemEntityId = systemEntityId;
	}

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Long getIdentityId() {
		return identityId;
	}

	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
}
