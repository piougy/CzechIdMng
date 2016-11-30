package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;

/**
 * Filter for identity role
 * 
 * @author svandav
 *
 */
public class IdentityRoleFilter extends QuickFilter {
	private UUID identityId;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

}
