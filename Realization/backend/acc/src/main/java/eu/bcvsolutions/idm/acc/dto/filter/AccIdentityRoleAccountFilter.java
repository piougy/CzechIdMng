package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for identity-role - accounts
 * 
 * @author Svanda
 *
 */
public class AccIdentityRoleAccountFilter implements BaseFilter, EntityAccountFilter {

	private UUID accountId;
	private UUID identityRoleId;
	private UUID systemId;
	private Boolean ownership;

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

	@Override
	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	@Override
	public Boolean isOwnership() {
		return ownership;
	}

	@Override
	public void setEntityId(UUID entityId) {
		this.identityRoleId = entityId;
	}

	public UUID getIdentityRoleId() {
		return identityRoleId;
	}

	public void setIdentityRoleId(UUID identityRoleId) {
		this.identityRoleId = identityRoleId;
	}
}
