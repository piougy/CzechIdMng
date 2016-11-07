package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for object on target system
 * 
 * @author Svanda
 *
 */
public class SchemaObjectClassFilter implements BaseFilter {
	
	private Long systemId;
	
	private String objectClassName;

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public String getObjectClassName() {
		return objectClassName;
	}

	public void setObjectClassName(String objectClassName) {
		this.objectClassName = objectClassName;
	}
}
