package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for attribute entity handling
 * 
 * @author Svanda
 *
 */
public class SchemaAttributeHandlingFilter implements BaseFilter {
	
	private UUID systemId;
	
	private UUID entityHandlingId;
	
	private UUID schemaAttributeId;
	
	private String idmPropertyName;

	public UUID getEntityHandlingId() {
		return entityHandlingId;
	}

	public void setEntityHandlingId(UUID entityHandlingId) {
		this.entityHandlingId = entityHandlingId;
	}

	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public UUID getSchemaAttributeId() {
		return schemaAttributeId;
	}

	public void setSchemaAttributeId(UUID schemaAttributeId) {
		this.schemaAttributeId = schemaAttributeId;
	}
	
}
