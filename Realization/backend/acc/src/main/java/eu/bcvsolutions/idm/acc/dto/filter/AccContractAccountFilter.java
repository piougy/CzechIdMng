package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for contract - accounts
 * 
 * @author Svanda
 *
 */
public class AccContractAccountFilter implements BaseFilter, EntityAccountFilter {

	private UUID accountId;
	private UUID contractId;
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

	public UUID getContractId() {
		return contractId;
	}

	public void setContractId(UUID contractId) {
		this.contractId = contractId;
	}

	@Override
	public Boolean isOwnership() {
		return ownership;
	}

	@Override
	public void setEntityId(UUID entityId) {
		this.contractId = entityId;
	}
	
	
}
