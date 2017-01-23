package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for attribute entity handling
 * 
 * @author Svanda
 *
 */
public class SystemAttributeMappingFilter implements BaseFilter {
	
	private UUID systemId;
	
	private UUID systemMappingId;
	
	private UUID schemaAttributeId;
	
	private String idmPropertyName;
	
	private Boolean isUid;

	public Boolean getIsUid() {
		return isUid;
	}

	public void setIsUid(Boolean isUid) {
		this.isUid = isUid;
	}

	public UUID getSystemMappingId() {
		return systemMappingId;
	}

	public void setSystemMappingId(UUID systemMappingId) {
		this.systemMappingId = systemMappingId;
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
