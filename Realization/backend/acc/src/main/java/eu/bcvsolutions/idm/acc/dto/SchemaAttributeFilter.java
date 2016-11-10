package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for attributes on target system
 * 
 * @author Svanda
 *
 */
public class SchemaAttributeFilter implements BaseFilter {
	
	private UUID objectClassId;
	private UUID systemId;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getObjectClassId() {
		return objectClassId;
	}

	public void setObjectClassId(UUID objectClassId) {
		this.objectClassId = objectClassId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

}
