package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for attributes on target system
 * 
 * @author Svanda
 *
 */
public class SchemaAttributeFilter implements BaseFilter {
	
	private Long objectClassId;
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getObjectClassId() {
		return objectClassId;
	}

	public void setObjectClassId(Long objectClassId) {
		this.objectClassId = objectClassId;
	}

}
