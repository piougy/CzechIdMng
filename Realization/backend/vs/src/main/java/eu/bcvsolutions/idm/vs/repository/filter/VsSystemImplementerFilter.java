package eu.bcvsolutions.idm.vs.repository.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for VS request implementer
 * 
 * @author Svanda
 *
 */

public class VsSystemImplementerFilter extends QuickFilter {

	UUID systemId;
	UUID identityId;


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
}
