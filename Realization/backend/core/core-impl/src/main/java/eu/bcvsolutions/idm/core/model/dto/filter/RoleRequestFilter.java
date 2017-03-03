package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Filter for role request
 * 
 * @author svandav
 *
 */
public class RoleRequestFilter extends QuickFilter {
	private UUID identityUUID;
	private RoleRequestState state;

	public UUID getIdentityUUID() {
		return identityUUID;
	}

	public void setIdentityUUID(UUID identityUUID) {
		this.identityUUID = identityUUID;
	}

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

}
