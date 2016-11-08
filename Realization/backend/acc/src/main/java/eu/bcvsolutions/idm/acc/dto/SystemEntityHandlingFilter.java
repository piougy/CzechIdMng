package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for system entity handling
 * 
 * @author Svanda
 *
 */
public class SystemEntityHandlingFilter implements BaseFilter {
	
	private Long systemId;

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}
}
