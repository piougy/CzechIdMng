package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Filter for concept role request
 * 
 * @author svandav
 *
 */
public class ConceptRoleRequestFilter extends QuickFilter {
	private UUID roleRequestId;
	private RoleRequestState state;
	private UUID identityRoleId;
	private UUID roleId;
	private UUID identityContractId;
	private UUID roleTreeNodeId;
	private ConceptRoleRequestOperation operation;
	
	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

	public UUID getIdentityRoleId() {
		return identityRoleId;
	}

	public void setIdentityRoleId(UUID identityRoleId) {
		this.identityRoleId = identityRoleId;
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public UUID getIdentityContractId() {
		return identityContractId;
	}

	public void setIdentityContractId(UUID identityContractId) {
		this.identityContractId = identityContractId;
	}

	public UUID getRoleTreeNodeId() {
		return roleTreeNodeId;
	}

	public void setRoleTreeNodeId(UUID roleTreeNodeId) {
		this.roleTreeNodeId = roleTreeNodeId;
	}

	public ConceptRoleRequestOperation getOperation() {
		return operation;
	}

	public void setOperation(ConceptRoleRequestOperation operation) {
		this.operation = operation;
	}

}
