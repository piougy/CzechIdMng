package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class AccountFilter implements BaseFilter {
	
	private UUID systemEntityId;
	
	private UUID systemId;
	
	private UUID identityId;
	
	private String uid;
	
	private String uidId; // For search exact same uid (not like as in uid field case)
	
	private AccountType accountType;
	
	public UUID getSystemEntityId() {
		return systemEntityId;
	}
	
	public void setSystemEntityId(UUID systemEntityId) {
		this.systemEntityId = systemEntityId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public String getUidId() {
		return uidId;
	}

	public void setUidId(String uidId) {
		this.uidId = uidId;
	}
	
}
