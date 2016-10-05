package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.model.dto.BaseFilter;

/**
 * Filter for entity on target system
 * 
 * @author Radek Tomiška
 *
 */
public class SystemEntityFilter implements BaseFilter {
	
	private Long systemId;
	
	private String uid;
	
	private SystemEntityType entityType; 

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}
}
