package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 *
 */
public class AccIdentityAccountFilter implements BaseFilter, EntityAccountFilter {

	private UUID accountId;
	private UUID identityId;
	private UUID roleId;
	private UUID systemId;
	private UUID identityRoleId;
	private UUID roleSystemId;
	private Boolean ownership;

	public Boolean isOwnership() {
		return ownership;
	}	

	@Override
	public void setOwnership(Boolean ownership) {
		this.ownership = ownership;
	}

	@Override
	public UUID getAccountId() {
		return accountId;
	}

	@Override
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
	
	@Override
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

	public UUID getRoleSystemId() {
		return roleSystemId;
	}

	public void setRoleSystemId(UUID roleSystemId) {
		this.roleSystemId = roleSystemId;
	}
	
	@Override
	public void setEntityId(UUID entityId) {
		this.identityId = entityId;
	}

}
