package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for system entity handling
 * 
 * @author Svanda
 *
 */
public class SystemEntityHandlingFilter implements BaseFilter {
	
	private UUID systemId;

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
}
