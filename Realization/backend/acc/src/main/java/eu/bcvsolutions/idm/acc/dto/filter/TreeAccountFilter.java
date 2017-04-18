package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for tree - accounts
 * 
 * @author Svanda
 *
 */
public class TreeAccountFilter implements BaseFilter, EntityAccountFilter {

	private UUID accountId;
	private UUID treeNodeId;
	private UUID systemId;
	private UUID identityRoleId;
	private UUID roleSystemId;
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

	public UUID getTreeNodeId() {
		return treeNodeId;
	}

	public void setTreeNodeId(UUID treeNodeId) {
		this.treeNodeId = treeNodeId;
	}
	
	
}
