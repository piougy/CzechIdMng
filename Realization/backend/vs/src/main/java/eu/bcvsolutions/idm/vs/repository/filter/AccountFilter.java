package eu.bcvsolutions.idm.vs.repository.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for vs account
 * 
 * @author Svanda
 *
 */

public class AccountFilter extends QuickFilter {

	String uid;
	UUID systemId;
	
	public void setUid(String uidValue) {
		this.uid = uidValue;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public UUID getSystemId() {
		return systemId;
	}
	
}
