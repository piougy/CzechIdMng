package eu.bcvsolutions.idm.vs.repository.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for VS request implementer
 * 
 * @author Svanda
 *
 */

public class VsRequestImplementerFilter extends QuickFilter {

	UUID requestId;
	UUID identityId;

	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}
}
