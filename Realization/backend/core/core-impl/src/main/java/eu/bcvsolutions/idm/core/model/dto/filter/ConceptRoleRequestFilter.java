package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Filter for concept role request
 * 
 * @author svandav
 *
 */
public class ConceptRoleRequestFilter extends QuickFilter {
	private UUID roleRequest;
	private RoleRequestState state;


	public UUID getRoleRequest() {
		return roleRequest;
	}

	public void setRoleRequest(UUID roleRequest) {
		this.roleRequest = roleRequest;
	}

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

}
