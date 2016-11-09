package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for entity on target system
 * 
 * @author Radek Tomiška
 *
 */
public class SystemEntityFilter implements BaseFilter {
	
	private UUID systemId;
	
	private String uid;
	
	private SystemEntityType entityType; 

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
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
