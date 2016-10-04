package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;

/**
 * Filter for role system mapping
 * 
 * @author Radek Tomiška
 *
 */
public class SystemEntityFilter implements BaseFilter {
	
	private Long systemId;

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}	
}
