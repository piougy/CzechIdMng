package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class AccAccountFilter extends DataFilter {
	
	private UUID systemEntityId;	
	private UUID systemId;	
	private UUID identityId;	
	private String uid;
	private AccountType accountType;
	private Boolean ownership;
	private Boolean supportChangePassword;
	private SystemEntityType entityType;  
	
	public AccAccountFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccAccountFilter(MultiValueMap<String, Object> data) {
		super(AccAccountDto.class, data);
	}
	
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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Boolean getOwnership() {
		return ownership;
	}

	public void setOwnership(Boolean ownership) {
		this.ownership = ownership;
	}

	public Boolean getSupportChangePassword() {
		return supportChangePassword;
	}

	public void setSupportChangePassword(Boolean supportChangePassword) {
		this.supportChangePassword = supportChangePassword;
	}
	
	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}
	
	public SystemEntityType getEntityType() {
		return entityType;
	}
}
